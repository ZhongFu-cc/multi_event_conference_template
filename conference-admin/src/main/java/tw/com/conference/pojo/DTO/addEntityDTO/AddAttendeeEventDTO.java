package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddAttendeeEventDTO {

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
	private Integer isPaid;

}
