package tw.com.conference.pojo.BO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.conference.enums.DiscountSourceEnum;

/**
 * 已套用的折扣明細，儲存於 orders.applied_discounts (JSON)<br>
 * <br>
 * 活動組合優惠與優惠碼為「擇一使用」，所以同一張訂單的清單只會有單一來源：<br>
 * 走組合優惠 → 每個命中的 package 各一筆<br>
 * 走優惠碼 → 固定只有一筆<br>
 * <br>
 * 欄位刻意採來源中立的命名 (sourceId / name)，讓前端用同一套邏輯渲染兩種來源。
 */
@Data
@NoArgsConstructor
public class AppliedDiscountBO {

	@Schema(description = "折扣來源; bundle=活動組合優惠, promo-code=優惠碼")
	private DiscountSourceEnum source;

	@Schema(description = "來源ID; BUNDLE 為 discountPackageId, PROMO_CODE 為 promoCodeId")
	private Long sourceId;

	@Schema(description = "顯示名稱; 組合優惠名稱 或 優惠碼活動名稱")
	private String name;

	@Schema(description = "折抵金額")
	private BigDecimal discount;

	@Schema(description = "優惠碼字串; 僅 PROMO_CODE 來源有值")
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

}
