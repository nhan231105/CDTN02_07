package campuswork.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class SeedMySql {
    private static final Pattern URL = Pattern.compile(
            "jdbc:mysql://([a-zA-Z0-9.-]+)(?::([0-9]+))?/([a-zA-Z0-9_]+)(?:\\?([^\\r\\n]*))?");

    private SeedMySql() {}

    record Target(String url, String username, String password) {}

    static Target target(Map<String, String> environment, boolean production, boolean render, boolean apply) {
        String url = required(environment, "CAMPUSWORK_SEED_JDBC_URL");
        var target = URL.matcher(url);
        if (!target.matches()) throw new IllegalArgumentException("Use a single-host MySQL JDBC URL.");
        if (production) {
             if (apply && !"approved-campuswork-demo-v1".equals(environment.get("CAMPUSWORK_SEED_PRODUCTION_CONFIRM"))) {
                throw new IllegalArgumentException("Production apply requires explicit operator approval after the production dry-run.");
            }
            var web = URL.matcher(required(environment, "DB_URL"));
            if (!web.matches() || !target.group(1).equalsIgnoreCase(web.group(1))
                    || port(target.group(2)) != port(web.group(2)) || !target.group(3).equals(web.group(3))) {
                throw new IllegalArgumentException("Seed and web database targets must match (host, port, schema).");
            }
            if (!verifiedTls(target.group(4))) {
                throw new IllegalArgumentException("Production seed requires sslMode=VERIFY_IDENTITY and a trusted server certificate.");
            }
        } else {
            if (!target.group(3).matches(".*_(staging|seed_test)")) {
                throw new IllegalArgumentException("Use a MySQL staging schema ending in _staging or _seed_test.");
            }
            if (!Set.of("localhost", "127.0.0.1").contains(target.group(1)) && !verifiedTls(target.group(4))) {
                throw new IllegalArgumentException("Remote staging requires sslMode=VERIFY_IDENTITY.");
            }
        }
        return new Target(url, required(environment, "CAMPUSWORK_SEED_DB_USERNAME"),
                required(environment, "CAMPUSWORK_SEED_DB_PASSWORD"));
    }

    private static boolean verifiedTls(String query) {
        return query != null && query.matches(
                "sslMode=VERIFY_IDENTITY(?:&(?:serverTimezone|connectionTimeZone)=[A-Za-z0-9_+/%:-]+)*");
    }

    private static int port(String value) {
        return value == null ? 3306 : Integer.parseInt(value);
    }

    private static String required(Map<String, String> environment, String key) {
        String value = environment.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Set " + key + " for the seed target.");
        return value;
    }

    static void checkSchema(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        var readOnly = new TransactionTemplate(manager);
        readOnly.setReadOnly(true);
        readOnly.executeWithoutResult(transaction -> {
            expect(jdbc, 4, """
                    SELECT COUNT(*) FROM information_schema.tables
                    WHERE table_schema = DATABASE() AND engine = 'InnoDB'
                    AND table_name IN ('users','employer_profiles','jobs','stored_files')
                    """, "Expected four InnoDB seed tables.");
            expect(jdbc, 0, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND character_set_name IS NOT NULL
                    AND character_set_name <> 'utf8mb4'
                    AND table_name IN ('users','employer_profiles','jobs','stored_files')
                    """, "Seed text columns must support utf8mb4.");
            expect(jdbc, 2, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND column_name = 'id'
                    AND table_name IN ('users','jobs') AND extra LIKE '%auto_increment%'
                    """, "Expected generated user and job IDs.");
            expect(jdbc, 1, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'role'
                    AND (data_type <> 'enum' OR column_type LIKE '%''EMPLOYER''%')
                    """, "User role must accept EMPLOYER.");
            expect(jdbc, 1, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'status'
                    AND (data_type <> 'enum' OR (column_type LIKE '%''ACTIVE''%' AND column_type LIKE '%''LOCKED''%'))
                    """, "User status must accept ACTIVE and LOCKED.");
            expect(jdbc, 1, """
                    SELECT COUNT(*) FROM information_schema.columns
                    WHERE table_schema = DATABASE() AND table_name = 'jobs' AND column_name = 'status'
                    AND (data_type <> 'enum' OR column_type LIKE '%''OPEN''%')
                    """, "Job status must accept OPEN.");
            expect(jdbc, 3, """
                    SELECT COUNT(DISTINCT CONCAT(table_name, '.', columns_in_index))
                    FROM (
                        SELECT table_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_index
                        FROM information_schema.statistics
                        WHERE table_schema = DATABASE() AND non_unique = 0
                        AND table_name IN ('users','stored_files')
                        GROUP BY table_name, index_name
                    ) AS indexes
                    WHERE (table_name = 'users' AND columns_in_index IN ('email','phone'))
                      OR (table_name = 'stored_files' AND columns_in_index = 'storage_name')
                    """, "Expected unique email, phone and storage name.");
            expect(jdbc, 4, """
                    SELECT COUNT(*) FROM information_schema.key_column_usage
                    WHERE constraint_schema = DATABASE() AND constraint_name = 'PRIMARY'
                    AND ((table_name IN ('users','jobs') AND column_name = 'id')
                      OR (table_name = 'employer_profiles' AND column_name = 'user_id')
                      OR (table_name = 'stored_files' AND column_name = 'ref'))
                    AND ordinal_position = 1
                    """, "Expected seed primary keys on id, user_id and ref.");
            expect(jdbc, 0, """
                    SELECT COUNT(*) FROM information_schema.triggers
                    WHERE trigger_schema = DATABASE()
                    AND event_object_table IN ('users','employer_profiles','jobs','stored_files')
                    """, "The seed tables have triggers; review their effects before seeding.");
            expect(jdbc, 3, """
                    SELECT COUNT(DISTINCT table_name) FROM information_schema.key_column_usage
                    WHERE constraint_schema = DATABASE() AND referenced_table_schema = DATABASE()
                    AND referenced_table_name = 'users' AND referenced_column_name = 'id'
                    AND ((table_name = 'jobs' AND column_name = 'employer_id')
                      OR (table_name = 'employer_profiles' AND column_name = 'user_id')
                      OR (table_name = 'stored_files' AND column_name = 'owner_id'))
                    """, "Expected owner foreign keys to users.id.");
        });
    }

    private static void expect(JdbcTemplate jdbc, int expected, String sql, String error) {
        Integer result = jdbc.queryForObject(sql, Integer.class);
        if (result == null || result != expected) throw new IllegalArgumentException(error);
    }
}
