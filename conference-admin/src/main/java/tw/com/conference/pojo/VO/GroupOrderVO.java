package tw.com.conference.pojo.VO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.pojo.BO.AppliedDiscountBO;
import tw.com.conference.pojo.BO.GroupMemberPriceBO;
import tw.com.conference.pojo.entity.GroupDiscountRule;

/**
 * 團體報名的金額試算 / 鎖定結果<br>
 * 試算時 ordersId 為 null；未達門檻時 eligible=false 且不會有折扣
 */
@Data
public class GroupOrderVO {

	@Schema(description = "團體ID")
	private Long registrationGroupId;

	@Schema(description = "訂單ID; 試算時為 null")
	private Long ordersId;

	@Schema(description = "人數 (含負責人)")
	private Integer memberCount;

	@Schema(description = "是否達到最低人數門檻; false 時不可鎖定")
	private Boolean eligible;

	@Schema(description = "最低人數門檻 (啟用中規則的最小 minCount)")
	private Integer minCount;

	@Schema(description = "套用的規則; 未達門檻為 null")
	private GroupDiscountRule appliedRule;

	@Schema(description = "下一個可升級的規則; 已是最高優惠為 null")
	private GroupDiscountRule nextRule;

	@Schema(description = "原價總額")
	private BigDecimal originalTotal;

	@Schema(description = "總折扣金額")
	private BigDecimal totalDiscount;

	@Schema(description = "應付金額")
	private BigDecimal finalPrice;

	@Schema(description = "折扣明細; 與個人報名結構相同, 團體報名固定一筆")
	private List<AppliedDiscountBO> appliedDiscounts = new ArrayList<>();

	@Schema(description = "逐人金額明細, 依加入順序")
	private List<GroupMemberPriceBO> memberPrices = new ArrayList<>();

}
