package tw.com.conference.pojo.BO;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;
import tw.com.conference.pojo.entity.PromoCampaign;
import tw.com.conference.pojo.entity.PromoCode;

/**
 * 優惠碼套用後的計算結果<br>
 * 由 PromoCodeHelper 產出，交給 RegistrationEventManager 落庫
 */
@Data
public class PromoCodeResultBO {

	/** 命中的優惠碼 */
	private PromoCode promoCode;

	/** 該碼所屬的活動定義 */
	private PromoCampaign promoCampaign;

	/** 原價總額 */
	private BigDecimal originalTotal;

	/** 總折抵金額 (已確保不超過 originalTotal) */
	private BigDecimal totalDiscount;

	/** 應付金額 (已確保不小於 0) */
	private BigDecimal finalPrice;

	/**
	 * 每場的折抵金額, key = eventId<br>
	 * 逐場型態才有值；訂單層態 (all_amount/all_percent) 所有值皆為 0
	 */
	private Map<Long, BigDecimal> discountByEventId;

}
