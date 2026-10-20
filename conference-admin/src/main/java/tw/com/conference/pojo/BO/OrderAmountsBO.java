package tw.com.conference.pojo.BO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Data;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.pojo.entity.PromoCampaign;
import tw.com.conference.pojo.entity.PromoCode;

/**
 * 報名金額的計算結果 (純計算，未落庫)<br>
 * <br>
 * 由 RegistrationEventManager 的試算與實際報名共用，
 * 確保 preview 與結帳算出的金額完全一致。
 */
@Data
public class OrderAmountsBO {

	/** 各場的價格規則 */
	private Map<Long, PricingRule> pricingRuleByEventId;

	/** 各場的活動資料 */
	private Map<Long, Event> eventById;

	/** 原價總額 */
	private BigDecimal originalTotal;

	/** 總折扣金額 */
	private BigDecimal totalDiscount;

	/** 應付金額 */
	private BigDecimal finalPrice;

	/** 折扣明細; 走組合優惠為每個命中的 package 各一筆, 走優惠碼固定一筆 */
	private List<AppliedDiscountBO> appliedDiscounts;

	/**
	 * 各場的折抵金額, key = eventId<br>
	 * 只有「免費報名」與「優惠碼的逐場型態」會有非 0 值；
	 * 組合優惠與優惠碼的訂單層態皆為 0 (折扣算在訂單層級，未分攤到單場)
	 */
	private Map<Long, BigDecimal> discountByEventId;

	/** 命中的優惠碼; null 代表本次走組合優惠路徑 */
	private PromoCode promoCode;

	/** 命中的優惠碼活動定義; null 代表本次走組合優惠路徑 */
	private PromoCampaign promoCampaign;

	/**
	 * 是否已付清 (應付 0 元即視為付款完成)
	 *
	 * @return
	 */
	public boolean isPaidOff() {
		return finalPrice.compareTo(BigDecimal.ZERO) == 0;
	}

	/**
	 * 本次是否使用了優惠碼
	 *
	 * @return
	 */
	public boolean hasPromoCode() {
		return promoCode != null;
	}

}
