package tw.com.conference.pojo.DTO.addEntityDTO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddEventDTO {

	@Schema(description = "父ID , 允許活動內有子活動選項")
	private Long parentId;

	@NotBlank
	@Schema(description = "活動主題")
	private String title;

	@NotBlank
	@Schema(description = "活動描述")
	private String description;

	@NotNull
	@Schema(description = "活動開始時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startAt;

	@NotNull
	@Schema(description = "活動結束時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endAt;

	@NotBlank
	@Schema(description = "活動地點")
	private String location;

	@NotNull
	@Schema(description = "人數限制,預設為0,0為不限人數")
	private Integer capacity;

}
