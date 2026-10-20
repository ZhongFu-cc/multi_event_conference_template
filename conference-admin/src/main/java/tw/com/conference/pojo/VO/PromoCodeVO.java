package tw.com.conference.pojo.VO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoRedemptionStatusEnum;

/**
 * 單一優惠碼及其使用情形
 */
@Data
public class PromoCodeVO {

	@Schema(description = "主鍵ID")
	private Long promoCodeId;

	@Schema(description = "優惠碼字串")
	private String code;

	@Schema(description = "可使用次數上限, 0 為不限")
	private Integer maxUses;

	@Schema(description = "已使用次數")
	private Integer usedCount;

	@Schema(description = "是否已使用過;0=否,1=是")
	private CommonStatusEnum isUsed;

	@Schema(description = "使用者會員ID; 未使用則為 null")
	private Long memberId;

	@Schema(description = "使用者姓名; 未使用則為 null")
	private String memberName;

	@Schema(description = "使用者E-Mail; 未使用則為 null")
	private String memberEmail;

	@Schema(description = "使用的訂單ID; 未使用則為 null")
	private Long ordersId;

	@Schema(description = "折抵金額; 未使用則為 null")
	private BigDecimal discountAmount;

	@Schema(description = "使用紀錄狀態; reserved=已占用待付款, redeemed=已使用")
	private PromoRedemptionStatusEnum redemptionStatus;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(description = "使用時間; 未使用則為 null")
	private LocalDateTime usedDate;

}
