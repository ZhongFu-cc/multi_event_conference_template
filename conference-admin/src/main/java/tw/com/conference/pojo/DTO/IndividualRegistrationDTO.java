package tw.com.conference.pojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 個人報名活動 / 報名金額試算
 */
@Data
public class IndividualRegistrationDTO {

	@NotEmpty(message = "活動事件 ID 列表不能為空")
	@Schema(description = "要報名的活動事件IDs")
	private List<Long> eventIds;

	@Schema(description = "優惠碼, 選填; 有填且有效時走優惠碼折抵, 不再套用活動組合優惠(兩者擇一)")
	private String promoCode;

}
