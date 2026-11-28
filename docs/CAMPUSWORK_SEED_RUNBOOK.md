# Chạy Seed Data CampusWork

Công cụ nằm trong JAR backend và chỉ được gọi khi đối số đầu tiên là `--campuswork-seed`. Bộ dữ liệu gồm 15 tài khoản nhà tuyển dụng, 15 hồ sơ, 15 logo PNG 512 × 512 và 40 tin part-time trả lương bằng VND theo giờ. Mỗi công ty có 2–3 tin. Tên công ty, tiêu đề tin, nội dung giới thiệu và logo đều có nhãn demo. Email dùng miền `.example`, số điện thoại `0000000001`–`0000000015` chỉ dùng làm định danh thử nghiệm. Tài khoản được tạo bởi công cụ nội bộ, với vai trò `EMPLOYER`, trạng thái `ACTIVE` và mật khẩu riêng được băm bằng BCrypt. Các địa chỉ giả lập không nhận OTP hoặc thư đặt lại mật khẩu.

## Chạy local

Mở terminal ở thư mục checkout `campuswork-vanle-review`. Dùng JDK 21 và Maven. Trên Windows dùng `mvnw.cmd` nếu Maven chưa có trong PATH. Trên Linux/macOS đổi thành `./mvnw` hoặc dùng Maven đã cài.

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
$seedJar = (Get-ChildItem target -Filter '*.jar' | Select-Object -First 1).FullName
java -jar $seedJar --campuswork-seed --dry-run
java -jar $seedJar --campuswork-seed --apply
java -jar $seedJar --campuswork-seed --apply
```

- Dry-run là mặc định khi bỏ `--apply`. Lệnh kiểm tra số công ty, phân bổ tin, dữ liệu theo DTO, ngày hết hạn, chữ ký/giải mã PNG, kích thước và nội dung ảnh trùng. Lệnh mặc định kiểm tra offline. Thêm `--mysql-test` hoặc `--mysql-production` để chạy kiểm tra MySQL chỉ đọc và phát hiện xung đột.
- `--apply` mặc định dùng H2 file riêng trong `secrets/campuswork-seed-test/`. Tool tự tạo schema **chỉ trong kho H2 thử nghiệm này**, đóng context sau khi chạy và không mở HTTP server. Lần đầu trên kho trống phải báo 15 công ty, 40 tin. Lần hai báo 0 công ty, 0 tin và 15 công ty bỏ qua.
- Ảnh đầu vào nằm ngoài JAR tại `db/seed-assets/logos/`. Có thể đổi thư mục bằng `CAMPUSWORK_SEED_LOGO_DIRECTORY`. Bytes ảnh đã nạp nằm trong DB. CSV nằm trong `secrets/campuswork-seed-test/credentials/accounts-*.csv`. Git đã loại trừ `secrets/`. CSV chỉ chứa các tài khoản vừa tạo và không được in ra terminal. Cùng thư mục có `receipt-*.csv` ghi user ID, logo ref và danh sách job ID của đúng lần chạy. Hai file có cùng hậu tố để đối chiếu. Khi mở bằng bảng tính, nhập cột phone dưới dạng văn bản để giữ số 0 ở đầu.
- CSV chứa mật khẩu dạng rõ để bàn giao nội bộ. Giữ trong thư mục riêng có quyền truy cập phù hợp. Trên Linux file được tạo với quyền `0600`, còn trên Windows file kế thừa ACL từ thư mục chứa. Đừng đưa CSV vào Git, log, thư mục upload hoặc gửi công khai.
- Nếu muốn xem seed bằng API local, đóng tool rồi chạy backend với profile `h2` và trỏ đúng file test vừa tạo. Dùng cùng thư mục làm việc với lần seed. Có thể dùng frontend hiện tại qua Vite proxy như README.

```powershell
java -jar $seedJar --spring.profiles.active=h2 --app.demo.seed=false --spring.datasource.url=jdbc:h2:file:./secrets/campuswork-seed-test/campuswork --spring.datasource.username=sa --file.upload.directory=./secrets/campuswork-seed-test/uploads
```

## Kiểm tra kết quả và xử lý chạy lại

- Kiểm tra `GET /api/jobs?jobType=PART_TIME&payType=HOURLY`, phân trang đủ để thấy các tin demo. Các tin demo có thời hạn 34–56 ngày tính từ ngày chạy tạo mới theo giờ Việt Nam. Ngày hết hạn không tự dời ở lần chạy lại.
- Lấy ID nhà tuyển dụng từ tin demo, kiểm tra `GET /api/employers/{id}/public` và `GET /api/employers/{id}/logo`. Tải logo phải trả `200` và `image/png`. Kiểm tra đăng nhập bằng một dòng trong CSV.
- Với mỗi công ty, UUID logo ổn định là dấu nhận diện trong `stored_files`. Khi thấy dấu này hoặc email/số điện thoại đã được sử dụng, tool bỏ qua toàn bộ công ty. Các tài khoản, hồ sơ, logo, tin và hạn tuyển đã có được giữ nguyên. Nếu một hồ sơ hoặc file đã thiếu, đối chiếu thủ công. Tool không tự bổ sung vào tài khoản đã có.
- Mọi bản ghi mới của một lần chạy nằm trong cùng transaction. Hai lần ghi cạnh tranh được DB bảo vệ bằng khóa UNIQUE của tài khoản và file. Nếu gặp lỗi xung đột khi đang ghi, lần chạy lỗi sẽ rollback. Metadata và bytes ảnh cùng rollback trong MySQL khi transaction thất bại.
- Nếu có `accounts-*.csv.pending` sau khi mất điện, tiến trình bị ngắt hoặc đổi tên CSV thất bại, đối chiếu kết quả trong DB trước khi dùng file. File có thể chứa thông tin của một lần commit chưa xác định. Giữ lại bản riêng để khôi phục nếu tài khoản đã được tạo. Nếu có file PNG tồn dư mà chưa có metadata, kiểm tra theo UUID và xử lý riêng trước khi thử lại.

## MySQL local và Render/Railway staging

MySQL dùng Hibernate `validate`. Với DB đã có dữ liệu, sao lưu rồi chạy [migration 09](../db/mysql/migration_09_stored_image_bytes.sql) trong đúng schema staging, trước khi chạy backend mới. Cài đặt mới dùng [schema.sql](../db/mysql/schema.sql). Migration thêm bảng `stored_image_bytes` với PK đồng thời là FK đến `stored_files.ref`, cùng charset và collation với khóa cha. `MEDIUMBLOB` chứa được ảnh bìa tới giới hạn 4 MiB hiện có. Migration chỉ tạo bảng, giữ nguyên metadata và profile. Không dùng thao tác xóa bảng để quay lui bản ứng dụng.

Backend lưu logo, ảnh bìa và avatar mới trong cùng transaction với metadata và profile. API, MIME và URL giữ nguyên. Khi ảnh cũ chưa có BLOB, backend vẫn thử đọc file theo metadata cũ. CV PDF tiếp tục dùng thư mục upload. Ảnh cũ chỉ còn ở `/tmp/uploads` cần được sao lưu trước restart hoặc redeploy nếu vẫn tải được. Chuyển ảnh cũ sang BLOB là bước backfill riêng, nên đối chiếu ref, MIME, kích thước và checksum trước khi ghi. Migration không khôi phục file đã mất.

Chạy seed từ máy quản trị để giữ CSV cục bộ. Cấp ba biến `CAMPUSWORK_SEED_JDBC_URL`, `CAMPUSWORK_SEED_DB_USERNAME`, `CAMPUSWORK_SEED_DB_PASSWORD` bên ngoài Git. Dùng schema có hậu tố `_staging` hoặc `_seed_test`. Kết nối staging từ ngoài máy cần `sslMode=VERIFY_IDENTITY` và CA được JVM tin cậy. JDBC URL không chứa tài khoản hoặc mật khẩu. Dùng tài khoản SELECT cho dry-run. Tài khoản apply cần SELECT, INSERT trên các bảng seed kể cả `stored_image_bytes`, và UPDATE trên `employer_profiles` để gắn logo.

```powershell
java -jar $seedJar --campuswork-seed --dry-run --mysql-test
java -jar $seedJar --campuswork-seed --apply --mysql-test
```

Kiểm tra login, danh sách việc và logo trên staging. Tải một logo qua API bằng tài khoản nhà tuyển dụng, ghi checksum, restart backend rồi tải lại và đối chiếu bytes. Kiểm tra cả 15 logo seed. Lưu backup Railway bao gồm bảng ảnh. Render Free không cần persistent disk để phục vụ ảnh đã lưu trong MySQL. [Render Free](https://render.com/docs/free) · [Railway Backups](https://docs.railway.com/volumes/backups)

## Audit chỉ đọc trước migration

Nếu production chưa có bảng BLOB, dùng [truy vấn audit chỉ đọc](../db/mysql/campuswork_seed_readonly_audit.sql) trong phiên MySQL đã chọn đúng schema. Truy vấn chạy cả trước migration và chỉ trả DDL, số lượng và cờ xung đột theo mã seed. Nó không yêu cầu chạy migration hoặc mở quyền ghi. So sánh DDL với schema trong repo, kiểm tra `stored_files.ref` là khóa tương thích, rồi đối chiếu kết quả trigger, FK và các cờ email, số điện thoại, UUID, tên file. Cờ trùng chỉ xác nhận có bản ghi, chưa chứng minh bản ghi thuộc lượt seed. Đối chiếu receipt trước khi quyết định. Dry-run Java dùng Hibernate validate, nên chạy ở staging sau khi áp dụng migration.
## Railway production sau khi có xác nhận

1. Hoàn tất staging và diễn tập khôi phục. Xác nhận schema thực tế trước khi áp dụng migration. Backup Railway phải bao gồm dữ liệu ảnh sau khi chuyển. Nếu ảnh cũ vẫn còn trên Render, sao lưu trước khi redeploy.
2. Sau phê duyệt riêng, DBA áp dụng migration 09 trước khi deploy bản backend mới. Kiểm tra tác vụ ghi ảnh và kích thước gói MySQL phù hợp giới hạn upload. Nếu phải quay lại backend cũ, giữ bảng BLOB và dữ liệu của nó. Backend cũ chỉ đọc file trên đĩa nên cần kế hoạch render các ảnh mới nếu quay lui.
3. Từ máy quản trị, đối chiếu host, port, schema của URL seed với datasource thực tế của Render. Truyền URL Render đã xác nhận qua `DB_URL` để tool so đích. Tool yêu cầu `sslMode=VERIFY_IDENTITY` cho JDBC seed. Nếu TCP Proxy chưa có chứng chỉ hợp lệ cho hostname, chỉ tiếp tục audit qua Railway SSH và Unix socket. Hoãn apply đến khi có kết nối JDBC xác minh danh tính.
4. Sau khi được phép đọc production, dùng tài khoản chỉ đọc chạy dry-run. Chỉ dùng tài khoản ghi tạm và biến xác nhận sau khi người dùng duyệt riêng bước apply.

```sh
java -jar campuswork.jar --campuswork-seed --dry-run --mysql-production

CAMPUSWORK_SEED_PRODUCTION_CONFIRM=approved-campuswork-demo-v1 \
  java -jar campuswork.jar --campuswork-seed --apply --mysql-production
```

5. Lưu hai CSV cùng hậu tố qua kênh riêng. Nghiệm thu theo receipt và so checksum ảnh trên Render sau một lần restart hoặc redeploy đã được lên lịch. Thu hồi quyền ghi tạm khi hoàn tất.
## Hoàn tác một lần seed

- Trước commit, tool tự rollback cả đợt và dọn file do đợt đó tạo. Khi kết quả commit chưa rõ hoặc file còn đuôi `.pending`, giữ file và đối chiếu DB trước khi chạy lại.
- Sau commit, dùng [SQL hoàn tác theo receipt](../db/mysql/campuswork_seed_rollback.sql) với quyền được phép sửa tài khoản/tin. Đây là hoàn tác về mặt hiển thị và truy cập. SQL chỉ xem xét đúng user ID, logo ref và 2–3 job ID của một dòng receipt. Nó kiểm tra quan hệ sở hữu và nhãn demo, từ chối nếu có thêm tin thuộc công ty ngoài receipt, rồi ẩn các tin trong receipt và khóa tài khoản demo, tăng phiên bản JWT. Các hàng hồ sơ, logo, tin, ứng tuyển và dữ liệu liên quan được giữ nguyên. Logo có URL công khai đã biết vẫn có thể tải được sau hoàn tác.
- Chạy bản SQL mặc định kết thúc bằng `ROLLBACK` trên staging trước. Đối chiếu từng dòng với receipt, xem `eligible`, danh sách ID và số bản ghi dự kiến. Chỉ khi được phép mới dùng bản riêng đổi câu cuối thành `COMMIT` cho từng công ty. Nếu có xung đột, giữ nguyên và xử lý qua admin theo ID. Phương án này không có lệnh xóa vật lý. Không chạy lại seed với kỳ vọng tự mở khóa hay hiện lại tin đã ẩn.
- Nếu cần gỡ vật lý vì một yêu cầu riêng, kiểm tra FK và dữ liệu phát sinh trước, tạo bản sao lưu theo các ID trong receipt và xin phê duyệt riêng. Không dùng tiêu đề hoặc tiền tố `[DEMO]` làm điều kiện xóa hàng loạt.

Phân tích ban đầu dùng ví dụ 33 tin. Manifest triển khai có 40 tin theo yêu cầu mới. [Báo cáo phân tích](CAMPUSWORK_SEED_DATA_ANALYSIS.md)
