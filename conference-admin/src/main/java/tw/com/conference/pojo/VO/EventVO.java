package tw.com.conference.pojo.VO;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;

@Data
public class EventVO {

	@Schema(description = "主鍵ID")
	private Long eventId;

	@Schema(description = "父ID , 允許活動內有子活動選項")
	private Long parentId;

	@Schema(description = "活動主題")
	private String title;

	@Schema(description = "活動描述")
	private String description;

	@Schema(description = "活動當天開始時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startAt;

	@Schema(description = "活動當天結束時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endAt;

	@Schema(description = "活動地點")
	private String location;

	@Schema(description = "人數限制,預設為0,0為不限人數")
	private Integer capacity;

	@Schema(description = "報名開放時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registrationOpenAt;

	@Schema(description = "報名關閉時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registrationCloseAt;

	@Schema(description = "是否為主活動;0=否,1=是;只允許有一個主活動")
	private CommonStatusEnum isMain;

	@Schema(description = "是否允許團體報名;0=否,1=是")
	private CommonStatusEnum allowGroupRegistration;

	@Schema(description = "是否啟用;0=否,1=是")
	private CommonStatusEnum isActive;
	
	@Schema(description = "當前報名人數")
	private Long currentCount;
	
	@Schema(description = "是否報名人數已滿;0=否,1=是")
	private CommonStatusEnum isFull;
	
	@Schema(description = "是否處於非報名時間;0=否,1=是")
	private CommonStatusEnum isOverdue;
	
	@Schema(description = "是否處於可報名狀態;0=否,1=是")
	private CommonStatusEnum isAvailable;
	
}
