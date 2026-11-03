-- Mô tả chi tiết (nhiều mục) cho tin demo. Chạy 1 lần trong Workbench sau seed.sql.
USE freelance_marketplace;

UPDATE jobs SET
 description='Quán cà phê tại trung tâm Quận 1 cần sinh viên phục vụ làm theo ca, lịch xếp linh hoạt theo thời khóa biểu.
Mô tả công việc:
- Chào đón, ghi order và phục vụ đồ uống cho khách
- Pha chế các món cơ bản theo công thức có sẵn
- Dọn bàn, giữ vệ sinh khu vực làm việc
- Phối hợp với quản lý ca để kiểm kê cuối ngày
Thời gian: 2-3 ca mỗi tuần, mỗi ca 4-5 giờ (sáng hoặc chiều).',
 requirements='- Sinh viên năm 1-4, đang học tại TP.HCM
- Nhanh nhẹn, trung thực, thân thiện với khách
- Đi làm đúng giờ, cam kết tối thiểu 3 tháng
- Không cần kinh nghiệm, được đào tạo từ đầu',
 benefits='- Lương 28.000đ/giờ, trả vào ngày 5 hàng tháng
- Ăn ca miễn phí, đồng phục do quán cấp
- Thưởng lễ tết, thưởng chuyên cần
- Đổi ca linh hoạt vào mùa thi (báo trước 3 ngày)'
 WHERE title='Nhân viên phục vụ quán cà phê';

UPDATE jobs SET
 description='Nhà hàng cần nhân viên phục vụ ca tối, phù hợp sinh viên có lịch học buổi sáng và chiều.
Mô tả công việc:
- Đón khách, hướng dẫn chỗ ngồi và giới thiệu thực đơn
- Ghi món, bưng bê và theo dõi bàn được phân công
- Hỗ trợ thanh toán và dọn bàn sau khi khách rời đi
Thời gian: 17h30 - 22h30, 3-4 buổi mỗi tuần.',
 requirements='- Ngoại hình gọn gàng, giao tiếp tốt
- Có thể làm việc vào buổi tối, kể cả cuối tuần
- Ưu tiên người từng làm dịch vụ nhưng không bắt buộc',
 benefits='- Lương 30.000đ/giờ cộng tip chia đều
- Ăn tối miễn phí tại nhà hàng
- Được đào tạo quy trình phục vụ chuyên nghiệp
- Cơ hội lên ca trưởng sau 3 tháng'
 WHERE title='Phục vụ nhà hàng ca tối';

UPDATE jobs SET
 description='Cửa hàng trà sữa tại Hải Châu cần bạn pha chế và hỗ trợ bán hàng.
Mô tả công việc:
- Nhận order và pha chế đồ uống theo công thức chuẩn
- Chuẩn bị nguyên liệu, topping trước giờ cao điểm
- Giữ quầy gọn gàng, vệ sinh dụng cụ
Thời gian: ca chiều hoặc tối, khoảng 20 giờ mỗi tuần.',
 requirements='- Không cần kinh nghiệm, được đào tạo 3 ngày
- Sạch sẽ, cẩn thận, chịu khó học hỏi
- Sinh viên các trường tại Đà Nẵng',
 benefits='- Lương 27.000đ/giờ
- Hỗ trợ giữ xe, thưởng chuyên cần
- Được uống thử sản phẩm mới
- Lịch làm linh hoạt theo lịch học'
 WHERE title='Pha chế trà sữa';

UPDATE jobs SET
 description='Cửa hàng tiện lợi tại Cầu Giấy tuyển thu ngân ca xoay, phù hợp sinh viên học ngày.
Mô tả công việc:
- Quét mã, thanh toán và xuất hóa đơn cho khách
- Sắp xếp, kiểm tra hạn sử dụng hàng hóa
- Hỗ trợ khách tìm sản phẩm, giữ cửa hàng sạch sẽ
- Kiểm quỹ cuối ca cùng quản lý',
 requirements='- Cẩn thận với tiền, tính nhẩm tốt
- Làm được ca chiều hoặc tối, tối thiểu 20 giờ/tuần
- Trung thực, có trách nhiệm',
 benefits='- Lương 27.000đ/giờ
- Giảm giá nhân viên, thưởng doanh số
- Đào tạo vận hành máy POS
- Có thể chuyển sang làm bán thời gian lâu dài'
 WHERE title='Thu ngân cửa hàng tiện lợi';

UPDATE jobs SET
 description='Tư vấn và bán hàng tại trung tâm thương mại vào thứ 7 và chủ nhật.
Mô tả công việc:
- Chào đón và tư vấn sản phẩm cho khách
- Giới thiệu chương trình khuyến mãi
- Hỗ trợ trưng bày và kiểm kê hàng',
 requirements='- Giao tiếp tốt, chịu được áp lực
- Làm được cả hai ngày cuối tuần
- Không cần kinh nghiệm',
 benefits='- Lương 180.000đ/ca
- Hoa hồng theo doanh số
- Được đào tạo kỹ năng bán hàng'
 WHERE title='Nhân viên bán hàng cuối tuần';

UPDATE jobs SET
 description='Hỗ trợ tổ chức sự kiện tại Công viên APEC.
Mô tả công việc:
- Phát tờ rơi, giới thiệu sản phẩm cho người tham dự
- Hướng dẫn khách tham gia trò chơi, check-in
- Hỗ trợ ban tổ chức dọn dẹp cuối ngày',
 requirements='- Nhiệt tình, ngoại hình ưa nhìn
- Có thể đứng và đi lại nhiều giờ ngoài trời
- Đến đúng giờ tập trung',
 benefits='- Lương 300.000đ/ngày, trả cuối ngày
- Cung cấp áo đồng phục và nước uống
- Được hỗ trợ ăn trưa'
 WHERE title='Phát tờ rơi và PG sự kiện';

UPDATE jobs SET
 description='Kho hàng thương mại điện tử cần nhân viên đóng gói đơn.
Mô tả công việc:
- Kiểm tra sản phẩm theo đơn hàng
- Đóng gói, dán nhãn và chuyển cho đơn vị vận chuyển
- Sắp xếp kho gọn gàng',
 requirements='- Cẩn thận, làm việc nhóm tốt
- Có thể làm ca sáng hoặc tối
- Sức khỏe tốt, có thể bê vác nhẹ',
 benefits='- Lương 26.000đ/giờ
- Thưởng đơn vượt chỉ tiêu
- Hỗ trợ cơm trưa'
 WHERE title='Nhân viên đóng gói đơn hàng online';

UPDATE jobs SET
 description='Cộng tác viên viết nội dung cho fanpage và TikTok của thương hiệu, làm việc từ xa.
Mô tả công việc:
- Lên ý tưởng và viết 15-20 bài mỗi tháng
- Viết caption, kịch bản video ngắn
- Theo dõi hiệu quả bài viết và đề xuất cải thiện',
 requirements='- Viết tốt, hiểu xu hướng mạng xã hội
- Có sản phẩm mẫu hoặc portfolio là lợi thế
- Chủ động, giao bài đúng hạn',
 benefits='- Thu nhập 3.000.000đ/tháng
- Thưởng bài lan tỏa
- Làm việc từ xa, tự sắp xếp thời gian
- Có thêm portfolio thực tế'
 WHERE title='Cộng tác viên Content mạng xã hội';

UPDATE jobs SET
 description='Trả lời tin nhắn khách và chốt đơn vào buổi tối, làm tại nhà.
Mô tả công việc:
- Tư vấn sản phẩm qua Messenger, Zalo
- Chốt đơn và chuyển thông tin cho bộ phận kho
- Báo cáo số đơn cuối ngày',
 requirements='- Gõ nhanh, kiên nhẫn, lịch sự
- Có máy tính hoặc điện thoại, mạng ổn định
- Làm được khung 19h-22h',
 benefits='- Lương 25.000đ/giờ
- Thưởng theo số đơn chốt
- Làm việc tại nhà'
 WHERE title='Trực page và chốt đơn';

UPDATE jobs SET
 description='Quay và dựng video ngắn giới thiệu quán, khoảng 8 video mỗi tháng.
Mô tả công việc:
- Lên ý tưởng, quay và dựng video 15-60 giây
- Chỉnh màu, thêm nhạc và phụ đề
- Bàn giao đúng lịch đăng đã thống nhất',
 requirements='- Biết CapCut hoặc Premiere
- Có thiết bị quay cơ bản
- Có sản phẩm video mẫu',
 benefits='- Thu nhập 4.000.000đ/tháng
- Được dùng sản phẩm của quán
- Chủ động thời gian làm việc'
 WHERE title='Quay dựng video TikTok cho quán';

UPDATE jobs SET
 description='Dạy kèm Toán lớp 9 tại nhà học sinh, 2 buổi mỗi tuần.
Mô tả công việc:
- Ôn tập kiến thức, giải bài tập theo chương trình
- Luyện đề ôn thi vào lớp 10
- Trao đổi tiến độ với phụ huynh hàng tháng',
 requirements='- Sinh viên sư phạm hoặc điểm Toán cao
- Kiên nhẫn, có phương pháp giải thích dễ hiểu
- Có thể dạy buổi tối hoặc cuối tuần',
 benefits='- Học phí 150.000đ/giờ
- Thưởng khi học sinh đạt kết quả tốt
- Được trung tâm cung cấp tài liệu'
 WHERE title='Gia sư Toán lớp 9';

UPDATE jobs SET
 description='Luyện giao tiếp tiếng Anh cho học sinh cấp 2, giáo trình có sẵn.
Mô tả công việc:
- Thực hành hội thoại theo chủ đề mỗi buổi
- Sửa phát âm và bổ sung từ vựng
- Nhận xét tiến bộ gửi phụ huynh',
 requirements='- IELTS 6.5 hoặc tương đương
- Phát âm rõ ràng, vui vẻ với trẻ
- Dạy được 2-3 buổi tối mỗi tuần',
 benefits='- Học phí 180.000đ/giờ
- Thưởng khi học sinh tiến bộ
- Được tập huấn phương pháp giảng dạy'
 WHERE title='Gia sư Tiếng Anh giao tiếp';

UPDATE jobs SET
 description='Hỗ trợ giảng viên trong lớp Scratch và Python cho trẻ em vào cuối tuần.
Mô tả công việc:
- Hướng dẫn học viên thực hành trên máy
- Hỗ trợ giải đáp lỗi cơ bản
- Điểm danh và giữ trật tự lớp',
 requirements='- Biết lập trình cơ bản
- Kiên nhẫn, yêu trẻ
- Làm được sáng thứ 7 và chủ nhật',
 benefits='- Lương 200.000đ/buổi
- Được đào tạo kỹ năng sư phạm
- Cấp giấy xác nhận hoạt động'
 WHERE title='Trợ giảng lớp lập trình thiếu nhi';

UPDATE jobs SET
 description='Thiết kế ấn phẩm truyền thông cho trung tâm, làm việc từ xa.
Mô tả công việc:
- Thiết kế poster, banner, bài đăng mạng xã hội
- Chỉnh sửa theo góp ý của bộ phận marketing
- Bàn giao file nguồn và file xuất',
 requirements='- Thành thạo Canva hoặc Photoshop
- Có gu thẩm mỹ, đúng deadline
- Có portfolio là lợi thế',
 benefits='- Thu nhập 2.500.000đ/tháng
- Có thêm portfolio thật
- Làm việc từ xa, giờ giấc linh hoạt'
 WHERE title='Thiết kế poster, banner part-time';

UPDATE jobs SET
 description='Phát triển giao diện web cho hệ thống quản lý học viên, làm từ xa.
Mô tả công việc:
- Xây dựng các trang giao diện bằng React
- Kết nối API do đội backend cung cấp
- Viết test và sửa lỗi theo review code',
 requirements='- Biết React, JavaScript, Git cơ bản
- Đọc hiểu tài liệu kỹ thuật tiếng Anh
- Dành tối thiểu 20 giờ/tuần',
 benefits='- Lương 6.000.000đ/tháng
- Được mentor kèm cặp
- Làm việc từ xa
- Có thể ký tiếp hợp đồng sau 6 tháng'
 WHERE title='Lập trình web React bán thời gian';

UPDATE jobs SET
 description='Viết test case và kiểm thử ứng dụng web của trung tâm.
Mô tả công việc:
- Viết kịch bản kiểm thử thủ công
- Ghi nhận và theo dõi lỗi
- Phối hợp với lập trình viên để xác nhận sửa lỗi',
 requirements='- Cẩn thận, tư duy logic
- Biết SQL là lợi thế
- Có thể làm việc tại văn phòng buổi chiều',
 benefits='- Hỗ trợ 4.500.000đ/tháng
- Chứng nhận thực tập
- Được hướng dẫn quy trình QA thực tế'
 WHERE title='Hỗ trợ kiểm thử phần mềm (QA)';
