package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 建立團體 (團體報名)<br>
 * 建立者即為負責人，一定出席且計入人數<br>
 * 團體名稱由系統依負責人資料自動產生，不開放自訂
 */
@Data
public class AddRegistrationGroupDTO {

	@NotNull
	@Schema(description = "要團體報名的活動事件ID; 一團只能報名一場")
	private Long eventId;

}
