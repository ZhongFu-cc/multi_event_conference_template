package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 團體報名時，單一成員的金額明細<br>
 * 價格依成員「鎖定當下」的會員身分與國籍決定，所以同團成員價格可能不同
 */
@Data
public class GroupMemberPriceBO {

	@Schema(description = "會員ID")
	private Long memberId;

	@Schema(description = "姓名")
	private String memberName;

	@Schema(description = "會員身分 (中文)")
	private String memberTypeLabel;

	@Schema(description = "原價")
	private BigDecimal originalPrice;

	@Schema(description = "此成員分到的團體折抵 = 原價 × 減免百分比 (四捨五入到元); 未達門檻為 0")
	private BigDecimal discount;

	@Schema(description = "此成員應付金額 = originalPrice - discount")
	private BigDecimal payableAmount;

}
