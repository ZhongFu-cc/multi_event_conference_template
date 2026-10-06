package tw.com.conference.pojo.DTO.putEntityDTO;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PutCheckinRecordDTO {

	@Schema(description = "主鍵ID")
	private Long checkinRecordId;

	@Schema(description = "與會者ID")
	private Long attendeesId;

	// 不開放修改 attendeeEventId:
	// 它只能由後端從 attendeeId + eventId 查出報名紀錄後寫入(見 CheckinRecordServiceImpl.resolvePaidAttendeeEvent)，
	// 若開放修改，等於可把報到紀錄改指到未報名/未繳費/別人的場次，繞過報到資格檢查。
	// 要更換場次請改用 undo-checkin 撤銷後，再以正確的 eventId 重新報到。

	@Schema(description = "簽到/退地點,保留欄位，未來擴展")
	private String location;

	@Schema(description = "動作類型, 1=簽到, 2=簽退")
	private Integer actionType;

	@Schema(description = "簽到/退時間")
	private LocalDateTime actionTime;

	@Schema(description = "備註")
	private String remark;

}
