package tw.com.conference.pojo.VO;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CheckinRecordVO {

	@Schema(description = "主鍵ID")
	private Long checkinRecordId;

	@Schema(description = "與會者VO對象(含基本資料)")
	private AttendeeVO attendeeVO;

	@Schema(description = "報名紀錄ID, 代表這筆報到屬於哪一場")
	private Long attendeeEventId;

	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "活動主題")
	private String eventTitle;

	@Schema(description = "簽到/退地點,保留欄位，未來擴展")
	private String location;

	@Schema(description = "動作類型, 1=簽到, 2=簽退")
	private Integer actionType;

	@Schema(description = "簽到/退時間")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime actionTime;

	@Schema(description = "備註")
	private String remark;

}
