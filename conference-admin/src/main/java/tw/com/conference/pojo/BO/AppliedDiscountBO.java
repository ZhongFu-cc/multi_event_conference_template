package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.conference.enums.DiscountSourceEnum;

/**
 * 已套用的折扣明細，儲存於 orders.applied_discounts (JSON)<br>
 * <br>
 * 活動組合優惠、優惠碼、團體報名優惠為「擇一使用」，所以同一張訂單的清單只會有單一來源：<br>
 * 走組合優惠 → 每個命中的 package 各一筆<br>
 * 走優惠碼 → 固定只有一筆<br>
 * 走團體報名 → 固定只有一筆<br>
 * <br>
 * 欄位刻意採來源中立的命名 (sourceId / name)，讓前端用同一套邏輯渲染各種來源。
 */
@Data
@NoArgsConstructor
public class AppliedDiscountBO {

	@Schema(description = "折扣來源; bundle=活動組合優惠, promo-code=優惠碼, group=團體報名優惠")
	private DiscountSourceEnum source;

	@Schema(description = "來源ID; BUNDLE 為 discountPackageId, PROMO_CODE 為 promoCodeId, GROUP 為 groupDiscountRuleId")
	private Long sourceId;

	@Schema(description = "顯示名稱; 組合優惠名稱、優惠碼活動名稱 或 團體優惠規則名稱")
	private String name;

	@Schema(description = "折抵金額")
	private BigDecimal discount;

	@Schema(description = "PROMO_CODE 為優惠碼字串, GROUP 為團體邀請碼; BUNDLE 無值")
	private String code;

	/**
	 * 建立「活動組合優惠」的折扣明細
	 *
	 * @param packageId   組合優惠ID
	 * @param packageName 組合優惠名稱
	 * @param discount    折抵金額
	 * @return
	 */
	public static AppliedDiscountBO ofBundle(Long packageId, String packageName, BigDecimal discount) {
		AppliedDiscountBO bo = new AppliedDiscountBO();
		bo.setSource(DiscountSourceEnum.BUNDLE);
		bo.setSourceId(packageId);
		bo.setName(packageName);
		bo.setDiscount(discount);
		return bo;
	}

	/**
	 * 建立「優惠碼」的折扣明細
	 *
	 * @param promoCodeId  優惠碼ID
	 * @param campaignName 優惠碼活動名稱
	 * @param code         會員實際輸入的碼
	 * @param discount     折抵金額
	 * @return
	 */
	public static AppliedDiscountBO ofPromoCode(Long promoCodeId, String campaignName, String code,
			BigDecimal discount) {
		AppliedDiscountBO bo = new AppliedDiscountBO();
		bo.setSource(DiscountSourceEnum.PROMO_CODE);
		bo.setSourceId(promoCodeId);
		bo.setName(campaignName);
		bo.setCode(code);
		bo.setDiscount(discount);
		return bo;
	}

	/**
	 * 建立「團體報名優惠」的折扣明細
	 *
	 * @param groupDiscountRuleId 命中的團體優惠規則ID
	 * @param ruleName            規則名稱
	 * @param inviteCode          團體的邀請碼，放在 code 讓前端/後台辨識是哪一團
	 * @param discount            折抵金額
	 * @return
	 */
	public static AppliedDiscountBO ofGroup(Long groupDiscountRuleId, String ruleName, String inviteCode,
			BigDecimal discount) {
		AppliedDiscountBO bo = new AppliedDiscountBO();
		bo.setSource(DiscountSourceEnum.GROUP);
		bo.setSourceId(groupDiscountRuleId);
		bo.setName(ruleName);
		bo.setCode(inviteCode);
		bo.setDiscount(discount);
		return bo;
	}

}
