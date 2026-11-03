-- Toạ độ cho việc làm và khu vực mong muốn của sinh viên (gợi ý việc gần nơi ở)
ALTER TABLE jobs ADD COLUMN lat DOUBLE NULL, ADD COLUMN lng DOUBLE NULL;
ALTER TABLE student_profiles ADD COLUMN lat DOUBLE NULL, ADD COLUMN lng DOUBLE NULL;

-- Dữ liệu mẫu: đặt tin ở quanh trung tâm từng thành phố (lệch nhẹ theo id để bản đồ không bị trùng điểm)
UPDATE jobs SET
  lat = CASE WHEN location IN ('TP. Hồ Chí Minh','TPHCM') THEN 10.7769 WHEN location = 'Hà Nội' THEN 21.0285 WHEN location = 'Đà Nẵng' THEN 16.0544 END
        + ((id * 37) % 100 - 50) / 1500,
  lng = CASE WHEN location IN ('TP. Hồ Chí Minh','TPHCM') THEN 106.7009 WHEN location = 'Hà Nội' THEN 105.8542 WHEN location = 'Đà Nẵng' THEN 108.2022 END
        + ((id * 53) % 100 - 50) / 1500
WHERE lat IS NULL AND location IN ('TP. Hồ Chí Minh','TPHCM','Hà Nội','Đà Nẵng');

UPDATE student_profiles SET lat = 10.7769, lng = 106.7009, preferred_area = COALESCE(NULLIF(preferred_area,''),'Quận 1, TP.HCM')
WHERE lat IS NULL AND preferred_area LIKE '%HCM%';
