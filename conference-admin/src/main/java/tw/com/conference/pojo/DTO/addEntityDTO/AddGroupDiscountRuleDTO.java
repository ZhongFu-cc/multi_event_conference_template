package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;

@Data
public class AddGroupDiscountRuleDTO {

	@NotNull
	@Schema(description = "活動事件ID")
	private Long eventId;

	@NotBlank
	@Schema(description = "規則名稱; 例: 5人以上9折")
	private String name;

	@NotNull
	@Min(2)
	@Max(1000)
	@Schema(description = "最低人數 (含負責人), 至少 2 人")
	private Integer minCount;

	@NotNull
	@Min(1)
	@Max(99)
	@Schema(description = "減免百分比 (1~99); 10 表示折抵 10% (即 9 折), 20 表示 8 折")
	private Integer discountValue;

	@Schema(description = "是否啟用;0=否,1=是; 不填預設啟用")
	private CommonStatusEnum isActive;

}
