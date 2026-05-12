package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.DiscountTypeEnum;

@Data
public class AddDiscountPackageRuleDTO {

	@NotNull
	@Schema(description = "組合優惠ID")
	private Long discountPackageId;

	@NotBlank
	@Schema(description = "規則名稱")
	private String name;

	@NotNull
	@Schema(description = "折扣類型 ; percent=百分比折扣 amount=固定折抵")
	private DiscountTypeEnum discountType;

	@NotNull
	@Schema(description = "折扣值 ; percent 填 15 表示 15%，amount 填折抵金額")
	private Integer discountValue;

	@NotNull
	@Schema(description = "從群組中選中幾個event時觸發")
	private Integer requiredCount;

	@Schema(description = "優先級,越大優先級越高 ; 同一批event命中多個折扣時，數字大的優先")
	private Integer priority;

}
