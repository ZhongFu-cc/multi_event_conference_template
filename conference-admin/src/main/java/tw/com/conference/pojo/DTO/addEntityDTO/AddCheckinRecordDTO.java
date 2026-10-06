package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCheckinRecordDTO {

	@NotNull
	@Schema(description = "與會者ID")
	private Long attendeesId;

	@NotNull
	@Schema(description = "活動事件ID, 由前端在報到畫面選擇場次後帶上")
	private Long eventId;

	@Schema(description = "簽到/退地點,保留欄位，未來擴展")
	private String location;

	@Schema(description = "動作類型, 1=簽到, 2=簽退")
	private Integer actionType;

}
