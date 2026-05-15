package tw.com.conference.pojo.DTO.addEntityDTO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;

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
	@Schema(description = "活動當天開始時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startAt;

	@NotNull
	@Schema(description = "活動當天結束時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endAt;

	@NotBlank
	@Schema(description = "活動地點")
	private String location;

	@NotNull
	@Schema(description = "人數限制,預設為0,0為不限人數")
	private Integer capacity;
	
	@Schema(description = "報名開放時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registrationOpenAt;

	@Schema(description = "報名關閉時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registrationCloseAt;

	@NotNull
	@Schema(description = "是否為主活動;0=否,1=是;只允許有一個主活動")
	private CommonStatusEnum isMain;

	@NotNull
	@Schema(description = "是否允許團體報名;0=否,1=是")
	private CommonStatusEnum allowGroupRegistration;


}
