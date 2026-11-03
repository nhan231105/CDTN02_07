package org.example.util;

import org.example.entity.Job;
import org.example.entity.PayType;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dò các dấu hiệu tin tuyển dụng đáng ngờ (thu phí/đặt cọc, lời mời phi thực tế,
 * mức lương bất thường) để cảnh báo sinh viên trước khi ứng tuyển. Đây chỉ là
 * gợi ý dựa trên từ khóa và ngưỡng lương, không thay thế việc admin xử lý báo cáo.
 */
public final class ScamSignals {
    private ScamSignals() {}

    public enum Level { NONE, LOW, HIGH }

    public record Result(Level level, List<String> flags) {
        public static final Result NONE_RESULT = new Result(Level.NONE, List.of());
    }

    private static final List<Pattern> FEE_PATTERNS = List.of(
            Pattern.compile("dat coc|dong coc|the chan"),
            Pattern.compile("giu (cmnd|can cuoc|chung minh|the sinh vien|bang goc)"),
            Pattern.compile("phi (ho so|dao tao|dong phuc|giu cho|xet duyet)"),
            Pattern.compile("(nop|dong|thu|chuyen khoan)[^.]{0,20}phi"),
            Pattern.compile("chuyen khoan truoc")
    );

    private static final List<Pattern> TOO_GOOD_PATTERNS = List.of(
            Pattern.compile("viec nhe luong cao"),
            Pattern.compile("khong can lam gi"),
            Pattern.compile("ngoi nha van co tien"),
            Pattern.compile("kiem tien online tai nha"),
            Pattern.compile("thu nhap khung")
    );

    private static String stripDiacritics(String text) {
        String normalized = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase(java.util.Locale.ROOT);
    }

    public static Result analyze(Job job) {
        if (job == null) return Result.NONE_RESULT;
        String text = stripDiacritics(String.join(" ",
                nullToEmpty(job.getTitle()), nullToEmpty(job.getDescription()),
                nullToEmpty(job.getRequirements()), nullToEmpty(job.getBenefits())));
        List<String> flags = new ArrayList<>();
        boolean high = false;
        for (Pattern p : FEE_PATTERNS) {
            if (p.matcher(text).find()) {
                flags.add("Có dấu hiệu yêu cầu đặt cọc, giữ giấy tờ hoặc thu phí trước khi làm việc");
                high = true;
                break;
            }
        }
        for (Pattern p : TOO_GOOD_PATTERNS) {
            if (p.matcher(text).find()) {
                flags.add("Lời mời nghe thiếu thực tế kiểu \"việc nhẹ lương cao\"");
                break;
            }
        }
        String salaryFlag = salaryAnomaly(job);
        if (salaryFlag != null) flags.add(salaryFlag);
        if (flags.isEmpty()) return Result.NONE_RESULT;
        return new Result(high ? Level.HIGH : Level.LOW, flags);
    }

    private static String salaryAnomaly(Job job) {
        BigDecimal salary = job.getSalary();
        PayType type = job.getPayType();
        if (salary == null || type == null) return null;
        double v = salary.doubleValue();
        boolean anomalous = switch (type) {
            case HOURLY -> v > 300_000;
            case SHIFT -> v > 1_000_000;
            case DAILY -> v > 2_000_000;
            case MONTHLY -> v > 30_000_000;
            case PROJECT -> false;
        };
        return anomalous ? "Mức lương cao bất thường so với việc làm bán thời gian, nên trao đổi kỹ trước khi nộp hồ sơ" : null;
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
