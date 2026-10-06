package tw.com.conference.pojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UndoCheckinDTO {

	@NotNull
	@Schema(description = "與會者ID")
	private Long attendeesId;

	@NotNull
	@Schema(description = "活動事件ID, 撤銷的是這位與會者在該場次的最後一筆簽到")
	private Long eventId;

}
