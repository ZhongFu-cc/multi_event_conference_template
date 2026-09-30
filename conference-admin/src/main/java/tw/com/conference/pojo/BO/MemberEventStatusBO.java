package tw.com.conference.pojo.BO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.conference.enums.CommonStatusEnum;

/**
 * 會員所報名的單一活動 及 其繳費狀態
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberEventStatusBO {

	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "活動主題")
	private String title;

	@Schema(description = "是否為主活動")
	private CommonStatusEnum isMain;

	@Schema(description = "是否付款;0=否,1=是")
	private CommonStatusEnum isPaid;

}
