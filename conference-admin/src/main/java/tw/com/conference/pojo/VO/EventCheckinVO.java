package tw.com.conference.pojo.VO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;

/**
 * 單場活動的報到名單<br>
 * 以該場「已繳費的報名紀錄」為基底，附上報到狀態
 */
@Data
public class EventCheckinVO {

	@Schema(description = "報名紀錄ID")
	private Long attendeeEventId;

	@Schema(description = "與會者ID")
	private Long attendeeId;

	@Schema(description = "會員ID")
	private Long memberId;

	@Schema(description = "與會者流水序號")
	private Integer sequenceNo;

	@Schema(description = "中文姓名")
	private String chineseName;

	@Schema(description = "名字")
	private String firstName;

	@Schema(description = "姓氏")
	private String lastName;

	@Schema(description = "E-Mail")
	private String email;

	@Schema(description = "單位(所屬的機構)")
	private String affiliation;

	@Schema(description = "職稱")
	private String jobTitle;

	@Schema(description = "是否已簽到過;0=否,1=是")
	private CommonStatusEnum isCheckedIn;

	@Schema(description = "是否仍在會場 (最後一筆動作為簽到);0=否,1=是")
	private CommonStatusEnum isOnSite;

	@Schema(description = "首次簽到時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime firstCheckinTime;

	@Schema(description = "最後一筆簽到/退時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastActionTime;

}
