package tw.com.conference.pojo.DTO.addEntityDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoCodeTypeEnum;
import tw.com.conference.enums.PromoDiscountTypeEnum;

@Data
public class AddPromoCampaignDTO {

	@NotBlank
	@Schema(description = "優惠碼活動名稱")
	private String name;

	@NotNull
	@Schema(description = "碼型態; general=單一通用碼不限次數, unique=批次產生每組限用一次")
	private PromoCodeTypeEnum codeType;

	@NotNull
	@Schema(description = "折扣型態; free/amount/percent 為逐場(看適用活動), all_amount/all_percent 為整張訂單")
	private PromoDiscountTypeEnum discountType;

	// free 型態不需要此欄位; percent/all_percent 需 1~100; amount/all_amount 需 >= 1
	// 型態與值的交叉檢查在 PromoCampaignServiceImpl.validate 內進行
	@Min(1)
	@Max(100000000)
	@Schema(description = "折扣值; free 不需填, amount/all_amount 為折抵金額, percent/all_percent 為減免百分比(1~100)")
	private BigDecimal discountValue;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "可使用起始日, 不填為不限")
	private LocalDate dateFrom;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "可使用結束日, 不填為不限")
	private LocalDate dateTo;

	@Schema(description = "是否啟用;0=否,1=是; 不填預設啟用")
	private CommonStatusEnum isActive;

	@Schema(description = "碼字串前綴, 選填; 例如 VIP 會產生 VIP-XXXXXXXX")
	private String codePrefix;

	@Min(1)
	@Max(5000)
	@Schema(description = "要產生幾組碼; codeType=unique 時必填, general 固定只產生 1 組")
	private Integer quantity;

	@Schema(description = "指定的碼字串; 僅 codeType=general 可用, 不填則自動產生")
	private String code;

	@Schema(description = "適用的活動事件IDs; 不填為全場適用。all_amount/all_percent 型態不可指定")
	private List<Long> eventIds;

}
