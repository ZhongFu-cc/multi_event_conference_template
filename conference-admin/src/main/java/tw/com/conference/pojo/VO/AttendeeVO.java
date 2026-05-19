package tw.com.conference.pojo.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.pojo.entity.Member;

@Data
public class AttendeeVO {

	@Schema(description = "主鍵ID")
	private Long attendeeId;

	@Schema(description = "主鍵ID")
	private Long memberId;

	@Schema(description = "參與者流水序號")
	private Integer sequenceNo;

	@Schema(description = "收據編號,客戶需要再透過excel模板批量匯入")
	private String receiptNo;

	@Schema(description = "會員資訊")
	private Member member;

	@Schema(description = "是否為去年與會者,true為是,false為否")
	private Boolean isLastYearAttendee = false;

}
