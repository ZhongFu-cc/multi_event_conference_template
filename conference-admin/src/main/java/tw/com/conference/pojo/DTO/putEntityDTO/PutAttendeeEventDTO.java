package tw.com.conference.pojo.DTO.putEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;

@Data
public class PutAttendeeEventDTO {

	@NotNull
	@Schema(description = "主鍵")
	private Long attendeeEventId;

	@NotNull
	@Schema(description = "與會者ID")
	private Long attendeeId;

	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "流水序號")
	private Integer sequenceNo;

	@Schema(description = "收據編號")
	private String receiptNo;

	@Schema(description = "是否付款;0=否,1=是")
	private CommonStatusEnum isPaid;

}
