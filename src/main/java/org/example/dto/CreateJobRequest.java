package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    @Size(min = 5, max = 200, message = "Tiêu đề phải từ 5 đến 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả công việc không được để trống")
    @Size(min = 20, max = 5000, message = "Mô tả phải từ 20 đến 5000 ký tự")
    private String description;

    @Size(max = 2000, message = "Yêu cầu không được vượt quá 2000 ký tự")
    private String requirements;

    @Size(max = 1000, message = "Lợi ích không được vượt quá 1000 ký tự")
    private String benefits;

    @Size(max = 100, message = "Danh mục không được vượt quá 100 ký tự")
    private String category;

    @Size(max = 100, message = "Địa điểm không được vượt quá 100 ký tự")
    private String location;

    @Size(max = 200, message = "Lịch làm việc không được vượt quá 200 ký tự")
    private String workSchedule;

    private BigDecimal salary;

    private LocalDate deadline;

    private Long employerId;
}
