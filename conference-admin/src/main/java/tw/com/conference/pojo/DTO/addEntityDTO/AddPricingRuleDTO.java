package tw.com.conference.pojo.DTO.addEntityDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.NationalityEnum;

@Data
public class AddPricingRuleDTO {

	@NotBlank
	@Schema(description = "規則別名；例:早鳥-本國-會員")
	private String alias;

	@NotNull
	@Schema(description = "活動事件ID")
	private Long eventId;

	@Schema(description = "會員身份ID，NULL = 適用所有身分")
	private Long memberTypeId;

	@NotBlank
	@Schema(description = "國籍，影響價格維度之一")
	private NationalityEnum nationality;

	@NotNull
	@Schema(description = "價格適用起始日")
	private LocalDate dateFrom;

	@NotNull
	@Schema(description = "優惠適用結束日")
	private LocalDate dateTo;

	@NotNull
	@Schema(description = "金額")
	private BigDecimal amount;

}
