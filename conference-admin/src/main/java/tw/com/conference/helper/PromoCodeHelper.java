package tw.com.conference.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.constants.I18nMessageKey;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoDiscountTypeEnum;
import tw.com.conference.exception.PromoCodeException;
import tw.com.conference.pojo.BO.PromoCodeResultBO;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.pojo.entity.PromoCampaign;
import tw.com.conference.pojo.entity.PromoCode;
import tw.com.conference.service.PromoCampaignService;
import tw.com.conference.service.PromoCodeEventService;
import tw.com.conference.service.PromoCodeService;

/**
 * 優惠碼的校驗與折抵計算<br>
 * <br>
 * 優惠碼與活動組合優惠 (discount_package) 為「擇一使用」 ——
 * 走這裡算出來的結果就不會再經過 {@link CalculateDiscountHelper}。<br>
 * <br>
 * 折扣型態分兩群 (見 {@link PromoDiscountTypeEnum})：<br>
 * 逐場型態：對「適用範圍 ∩ 報名場次」的每一場各自折抵<br>
 * 訂單層態：忽略適用範圍，直接折抵原價總額
 */
@Component
@RequiredArgsConstructor
public class PromoCodeHelper {

	private static final BigDecimal HUNDRED = new BigDecimal(100);

	private final MessageHelper messageHelper;
	private final PromoCodeService promoCodeService;
	private final PromoCampaignService promoCampaignService;
	private final PromoCodeEventService promoCodeEventService;

	/**
	 * 校驗優惠碼並計算折抵<br>
	 * <b>不占用額度</b> —— 額度占用由呼叫端在落庫前執行 {@code promoCodeService.reserveQuota}
	 *
	 * @param code                 會員輸入的碼
	 * @param eventIds             本次報名的活動
	 * @param pricingRuleByEventId 各場的價格規則
	 * @return 計算結果
	 */
	public PromoCodeResultBO resolve(String code, List<Long> eventIds,
			Map<Long, PricingRule> pricingRuleByEventId) {

		// 1.找碼
		PromoCode promoCode = promoCodeService.getByCode(code);
		if (promoCode == null) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.NOT_FOUND));
		}

		// 2.找活動定義並校驗狀態與期間
		PromoCampaign promoCampaign = promoCampaignService.get(promoCode.getPromoCampaignId());
		if (promoCampaign == null) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.NOT_FOUND));
		}
		this.validateCampaign(promoCampaign);

		// 3.額度檢查 (僅查詢; 真正占用時還會再以條件式 UPDATE 確認一次)
		if (!promoCodeService.hasQuota(promoCode)) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.EXHAUSTED));
		}

		// 4.算原價總額
		BigDecimal originalTotal = eventIds.stream()
				.map(eventId -> pricingRuleByEventId.get(eventId).getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		PromoDiscountTypeEnum discountType = promoCampaign.getDiscountType();

		// 5.依型態算折抵
		Map<Long, BigDecimal> discountByEventId = new HashMap<>();
		BigDecimal totalDiscount;

		if (discountType.isPerEvent()) {
			totalDiscount = this.calcPerEventDiscount(promoCampaign, eventIds, pricingRuleByEventId,
					discountByEventId);
		} else {
			// 訂單層態：忽略適用範圍，各場的折抵維持 0
			eventIds.forEach(eventId -> discountByEventId.put(eventId, BigDecimal.ZERO));
			totalDiscount = this.calcOrderLevelDiscount(promoCampaign, originalTotal);
		}

		// 6.折抵維持優惠碼原本的金額，只保護應付不低於 0
		BigDecimal finalPrice = originalTotal.subtract(totalDiscount).max(BigDecimal.ZERO);

		PromoCodeResultBO result = new PromoCodeResultBO();
		result.setPromoCode(promoCode);
		result.setPromoCampaign(promoCampaign);
		result.setOriginalTotal(originalTotal);
		result.setTotalDiscount(totalDiscount);
		result.setFinalPrice(finalPrice);
		result.setDiscountByEventId(discountByEventId);

		return result;
	}

	/**
	 * 校驗優惠碼活動的啟用狀態與可使用期間
	 *
	 * @param promoCampaign
	 */
	private void validateCampaign(PromoCampaign promoCampaign) {

		if (!CommonStatusEnum.YES.equals(promoCampaign.getIsActive())) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.INACTIVE));
		}

		LocalDate today = LocalDate.now();

		if (promoCampaign.getDateFrom() != null && today.isBefore(promoCampaign.getDateFrom())) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.NOT_STARTED));
		}

		if (promoCampaign.getDateTo() != null && today.isAfter(promoCampaign.getDateTo())) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.EXPIRED));
		}
	}

	/**
	 * 逐場型態 (FREE / AMOUNT / PERCENT) 的折抵<br>
	 * 對「適用範圍 ∩ 報名場次」的每一場各自折抵，折抵維持優惠碼原本的金額
	 *
	 * @param promoCampaign
	 * @param eventIds
	 * @param pricingRuleByEventId
	 * @param discountByEventId    輸出參數，逐場折抵會寫入此 Map
	 * @return 折抵總額
	 */
	private BigDecimal calcPerEventDiscount(PromoCampaign promoCampaign, List<Long> eventIds,
			Map<Long, PricingRule> pricingRuleByEventId, Map<Long, BigDecimal> discountByEventId) {

		// 適用範圍; 空集合 = 全場適用
		Set<Long> scope = promoCodeEventService.findEventIdsByCampaignId(promoCampaign.getPromoCampaignId());
		boolean applyToAll = scope.isEmpty();

		// 範圍與本次報名活動無交集時直接擋下，
		// 否則會員會以原價付款卻白白消耗一組碼
		if (!applyToAll && eventIds.stream().noneMatch(scope::contains)) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.NOT_APPLICABLE));
		}

		BigDecimal totalDiscount = BigDecimal.ZERO;

		for (Long eventId : eventIds) {
			BigDecimal originalPrice = pricingRuleByEventId.get(eventId).getAmount();
			BigDecimal discount = BigDecimal.ZERO;

			if (applyToAll || scope.contains(eventId)) {
				discount = switch (promoCampaign.getDiscountType()) {
					case FREE -> originalPrice;
					case AMOUNT -> promoCampaign.getDiscountValue();
					case PERCENT -> originalPrice.multiply(promoCampaign.getDiscountValue())
							.divide(HUNDRED, 0, RoundingMode.HALF_UP);
					// 訂單層態不會走到這裡 (由 isPerEvent 分流)
					default -> BigDecimal.ZERO;
				};
			}

			discountByEventId.put(eventId, discount);
			totalDiscount = totalDiscount.add(discount);
		}

		return totalDiscount;
	}

	/**
	 * 訂單層態 (ALL_AMOUNT / ALL_PERCENT) 的折抵<br>
	 * 忽略適用範圍，直接對原價總額折抵
	 *
	 * @param promoCampaign
	 * @param originalTotal
	 * @return 折抵總額
	 */
	private BigDecimal calcOrderLevelDiscount(PromoCampaign promoCampaign, BigDecimal originalTotal) {

		return switch (promoCampaign.getDiscountType()) {
			case ALL_AMOUNT -> promoCampaign.getDiscountValue();
			case ALL_PERCENT -> originalTotal.multiply(promoCampaign.getDiscountValue())
					.divide(HUNDRED, 0, RoundingMode.HALF_UP);
			// 逐場型態不會走到這裡 (由 isPerEvent 分流)
			default -> BigDecimal.ZERO;
		};
	}

}
