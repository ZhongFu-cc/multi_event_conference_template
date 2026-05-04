package tw.com.conference.pojo.DTO.putEntityDTO;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PutEventDTO {

	@Schema(description = "主鍵ID")
	private Long eventId;

	@Schema(description = "父ID , 允許活動內有子活動選項")
	private Long parentId;

	@Schema(description = "活動主題")
	private String title;

	@Schema(description = "活動描述")
	private String description;

	@Schema(description = "活動開始時間")
	private LocalDateTime startAt;

	@Schema(description = "活動結束時間")
	private LocalDateTime endAt;

	@Schema(description = "活動地點")
	private String location;

	@Schema(description = "人數限制,預設為0,0為不限人數")
	private Integer capacity;

	@Schema(description = "活動啟用狀態,預設為0,啟用")
	private Integer isActive;

}
