package tw.com.conference.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.constants.I18nMessageKey;
import tw.com.conference.constants.OrderConstants;
import tw.com.conference.convert.EventConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.exception.EventException;
import tw.com.conference.exception.PricingRuleException;
import tw.com.conference.exception.PromoCodeException;
import tw.com.conference.helper.AttendeeAdmissionHelper;
import tw.com.conference.helper.CalculateDiscountHelper;
import tw.com.conference.helper.PromoCodeHelper;
import tw.com.conference.helper.MessageHelper;
import tw.com.conference.pojo.BO.AppliedDiscountBO;
import tw.com.conference.pojo.BO.CalculateResultBO;
import tw.com.conference.pojo.BO.EventPriceBO;
import tw.com.conference.pojo.BO.OrderAmountsBO;
import tw.com.conference.pojo.BO.PromoCodeResultBO;
import tw.com.conference.pojo.DTO.GroupRegistrationDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderItemDTO;
import tw.com.conference.pojo.VO.EventOrderVO;
import tw.com.conference.pojo.VO.EventVO;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.EventService;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.service.OrdersItemService;
import tw.com.conference.service.OrdersService;
import tw.com.conference.service.PricingRuleService;
import tw.com.conference.service.PromoCodeRedemptionService;
import tw.com.conference.service.PromoCodeService;
import tw.com.conference.utils.CountryUtil;

/**
 * 用於處理會員報名活動的處理
 */
@Component
@RequiredArgsConstructor
public class RegistrationEventManager {

	private final EventService eventService;
	private final EventConvert eventConvert;
	private final OrdersService ordersService;
	private final OrdersItemService ordersItemService;
	private final MemberTypeService memberTypeService;
	private final AttendeeEventService attendeeEventService;
	private final PricingRuleService pricingRuleService;
	private final CalculateDiscountHelper calculateDiscountHelper;
	private final AttendeeAdmissionHelper attendeeAdmissionHelper;
	private final MessageHelper messageHelper;
	private final PromoCodeHelper promoCodeHelper;
	private final PromoCodeService promoCodeService;
	private final PromoCodeRedemptionService promoCodeRedemptionService;

	/**
	 * 獲取當下可報名的Event<br>
	 * 以isAvailable為主要條件
	 */
	public List<EventVO> findAvailableEvent() {

		// 1.先拿到所有活動
		List<Event> allEvents = eventService.list();
		if (allEvents.isEmpty()) {
			return Collections.emptyList();
		}

		LocalDateTime now = LocalDateTime.now();

		// 2.遍歷活動進行過濾，根據資格設定 isAvailable
		return allEvents.stream().map(event -> {
			EventVO eventVO = eventConvert.entityToVO(event);

			// 2-1直接查當前人數 
			long currentCount = attendeeEventService.countByEventId(event.getEventId());
			eventVO.setCurrentCount(currentCount);

			// 2-2判斷狀態
			// 限制人數不為0時,且當前人數大於限制人數
			CommonStatusEnum isActive = event.getIsActive();
			CommonStatusEnum isFull = CommonStatusEnum
					.fromBoolean(!event.getCapacity().equals(0) && currentCount >= event.getCapacity());
			// 尚未開放 或 已截止 皆視為逾期
			CommonStatusEnum isOverdue = CommonStatusEnum.fromBoolean(
					now.isBefore(event.getRegistrationOpenAt()) || now.isAfter(event.getRegistrationCloseAt()));

			eventVO.setIsFull(isFull);
			eventVO.setIsOverdue(isOverdue);

			// 2-3. 綜合判斷：啟用中(YES) + 未逾期(NO) + 未滿員(NO) = 可報名
			// 或者是：isActive == YES && isOverdue == NO && isFull == NO
			boolean availableResult = (isActive.equals(CommonStatusEnum.YES)) && (isOverdue.equals(CommonStatusEnum.NO))
					&& (isFull.equals(CommonStatusEnum.NO));

			// 2-4. 設置 eventVO 的 isAvailable
			eventVO.setIsAvailable(CommonStatusEnum.fromBoolean(availableResult));

			return eventVO;
		}).toList();

	}

	/**
	 * 免費報名活動 (後台新增會員 / 現場報到 使用)<br>
	 * 訂單金額直接為 0 且視為付款完成，報名紀錄直接為已付款，並直接成為與會者
	 *
	 * @param member
	 * @param eventIds
	 */
	public EventOrderVO freeRegistration(Member member, List<Long> eventIds) {
		return this.registerEvents(member, eventIds, true);
	}

	/**
	 * 報名活動的核心流程<br>
	 * 解析價格規則 → 計算折扣 → 建立訂單與細項 → 建立報名紀錄 → (若已付清) 成為與會者
	 *
	 * @param member   會員
	 * @param eventIds 要報名的活動
	 * @param isFree   是否為免費訂單 (全額折抵, 直接付款完成)
	 * @return
	 */
	@Transactional
	public EventOrderVO registerEvents(Member member, List<Long> eventIds, boolean isFree) {
		return this.registerEvents(member, eventIds, null, isFree);
	}

	/**
	 * 報名活動的核心流程 (可帶優惠碼)<br>
	 * 解析價格規則 → 套用折扣(優惠碼或組合優惠，擇一) → 建立訂單與細項 → 建立報名紀錄 → (若已付清) 成為與會者
	 *
	 * @param member    會員
	 * @param eventIds  要報名的活動
	 * @param promoCode 優惠碼，可為 null/空白; isFree 時一律忽略
	 * @param isFree    是否為免費訂單 (全額折抵, 直接付款完成)
	 * @return
	 */
	@Transactional
	public EventOrderVO registerEvents(Member member, List<Long> eventIds, String promoCode, boolean isFree) {

		// 如果沒有活動則返回
		if (eventIds == null || eventIds.isEmpty()) {
			return null;
		}

		// 1.計算金額 (與試算端點共用同一段邏輯，確保兩邊金額一致)
		OrderAmountsBO amounts = this.resolveOrderAmounts(member, eventIds, promoCode, isFree);

		// 2.有使用優惠碼時先占用額度; 條件式 UPDATE 回 0 列代表已被別人用完
		if (amounts.hasPromoCode()
				&& !promoCodeService.reserveQuota(amounts.getPromoCode().getPromoCodeId())) {
			throw new PromoCodeException(messageHelper.get(I18nMessageKey.PromoCode.EXHAUSTED));
		}

		// 3.建立訂單
		AddOrderDTO addOrderDTO = new AddOrderDTO();
		addOrderDTO.setStatus(amounts.isPaidOff() ? OrderStatusEnum.PAYMENT_SUCCESS : OrderStatusEnum.UNPAID);
		addOrderDTO.setMemberId(member.getMemberId());
		// 綠界付款頁只能顯示商品概要，用 # 分段
		addOrderDTO.setItemsSummary(eventIds.stream()
				.map(eventId -> amounts.getEventById().get(eventId).getTitle())
				.collect(Collectors.joining("#")));
		addOrderDTO.setOriginalTotalAmount(amounts.getOriginalTotal());
		addOrderDTO.setTotalDiscountAmount(amounts.getTotalDiscount());
		addOrderDTO.setTotalAmount(amounts.getFinalPrice());
		addOrderDTO.setAppliedDiscounts(amounts.getAppliedDiscounts());
		if (isFree) {
			addOrderDTO.setRemark(OrderConstants.REMARK_FREE_BY_ADMIN);
		}
		Long orderId = ordersService.addOrder(addOrderDTO);

		// 4.建立訂單細項
		for (Long eventId : eventIds) {
			Event event = amounts.getEventById().get(eventId);
			BigDecimal originalPrice = amounts.getPricingRuleByEventId().get(eventId).getAmount();
			BigDecimal discount = amounts.getDiscountByEventId().getOrDefault(eventId, BigDecimal.ZERO);

			AddOrderItemDTO addOrderItemDTO = new AddOrderItemDTO();
			addOrderItemDTO.setOrdersId(orderId);
			addOrderItemDTO.setEventId(eventId);
			addOrderItemDTO.setProductType(OrderConstants.PRODUCT_TYPE_EVENT);
			addOrderItemDTO.setProductName(event.getTitle());
			addOrderItemDTO.setQuantity(1);
			// 單價一律記原價，折抵另記，這樣繳費證明才能逐項顯示「原價 X / 折抵 -Y」
			addOrderItemDTO.setUnitPrice(originalPrice);
			addOrderItemDTO.setDiscount(discount);
			addOrderItemDTO.setSubtotal(originalPrice.subtract(discount).max(BigDecimal.ZERO));

			ordersItemService.addOrderItem(addOrderItemDTO);
		}

		// 5.有使用優惠碼時建立使用紀錄 (狀態 reserved，付款成功後才轉 redeemed)
		if (amounts.hasPromoCode()) {
			promoCodeRedemptionService.createReserved(amounts.getPromoCode().getPromoCodeId(),
					amounts.getPromoCampaign().getPromoCampaignId(), member.getMemberId(), orderId,
					amounts.getTotalDiscount());
		}

		// 6.報名剛剛選擇參加的活動
		if (amounts.isPaidOff()) {
			attendeeEventService.batchCreatePaidRecords(member.getMemberId(), eventIds);
			// 已付清，成為與會者
			attendeeAdmissionHelper.admitAfterPayment(member);
		} else {
			attendeeEventService.batchCreateUnpaidRecords(member.getMemberId(), eventIds);
			// 標記未付款，供後台篩選與批次寄催款信
			attendeeAdmissionHelper.markUnpaid(member.getMemberId());
		}

		return this.toEventOrderVO(eventIds, amounts);
	}

	/**
	 * 報名金額試算<br>
	 * 與 {@link #registerEvents} 共用同一段計算，但<b>不建立訂單、不占用優惠碼額度</b>。<br>
	 * <br>
	 * 注意：此處的優惠碼額度只做「查詢」。真正結帳時仍可能因額度被他人搶走而失敗，
	 * 前端需要能處理結帳階段才出現的額度錯誤。
	 *
	 * @param member    會員
	 * @param eventIds  要報名的活動
	 * @param promoCode 優惠碼，可為 null/空白
	 * @return
	 */
	public EventOrderVO previewRegistration(Member member, List<Long> eventIds, String promoCode) {

		if (eventIds == null || eventIds.isEmpty()) {
			return null;
		}

		OrderAmountsBO amounts = this.resolveOrderAmounts(member, eventIds, promoCode, false);

		return this.toEventOrderVO(eventIds, amounts);
	}

	/**
	 * 計算報名金額 (純計算，不寫入任何資料)<br>
	 * <br>
	 * 折扣來源為<b>擇一</b>：有輸入有效的優惠碼就走優惠碼，完全不跑活動組合優惠；
	 * 沒有優惠碼才跑組合優惠。
	 *
	 * @param member    會員
	 * @param eventIds  要報名的活動
	 * @param promoCode 優惠碼，可為 null/空白; isFree 時一律忽略
	 * @param isFree    是否為免費訂單
	 * @return
	 */
	private OrderAmountsBO resolveOrderAmounts(Member member, List<Long> eventIds, String promoCode, boolean isFree) {

		// 一個會員對同一個活動只能有一筆報名 / 一張訂單，已報名過的活動直接擋下
		Set<Long> registeredEventIds = attendeeEventService.findByMemberId(member.getMemberId())
				.stream()
				.map(AttendeeEvent::getEventId)
				.collect(Collectors.toSet());
		for (Long eventId : eventIds) {
			if (registeredEventIds.contains(eventId)) {
				throw new EventException(messageHelper.get(I18nMessageKey.Registration.EVENT_ALREADY_REGISTERED,
						eventService.get(eventId).getTitle()));
			}
		}

		// 先獲取會員身分
		MemberType memberType = memberTypeService.get(member.getMemberTypeId());
		// 獲取會員國籍
		NationalityEnum nationalityEnum = CountryUtil.getDomesticOrInternational(member.getCountry());
		// 獲取當前時間
		LocalDateTime now = LocalDateTime.now();

		// 初始化一個以eventId為key,PricingRule為值的Map對象
		Map<Long, PricingRule> pricingRuleByEventId = new HashMap<>();
		Map<Long, Event> eventById = new HashMap<>();

		// 遍歷他所報名的活動,搭配會員的身分及報名時間,得到當前匹配的價格規則 (該場活動要付的錢)
		for (Long eventId : eventIds) {
			PricingRule pricingRule;
			try {
				// 找到當前匹配的價格規則
				pricingRule = pricingRuleService.resolvePricingRule(eventId, memberType.getMemberTypeId(),
						nationalityEnum, now);
			} catch (PricingRuleException e) {
				// 免費報名 (後台新增/現場報到) 不受報名時段限制，沒有規則時以 0 元記錄
				if (!isFree) {
					throw e;
				}
				pricingRule = new PricingRule();
				pricingRule.setEventId(eventId);
				pricingRule.setAmount(BigDecimal.ZERO);
			}
			pricingRuleByEventId.put(eventId, pricingRule);
			eventById.put(eventId, eventService.get(eventId));
		}

		OrderAmountsBO amounts = new OrderAmountsBO();
		amounts.setPricingRuleByEventId(pricingRuleByEventId);
		amounts.setEventById(eventById);

		BigDecimal originalTotal = eventIds.stream()
				.map(eventId -> pricingRuleByEventId.get(eventId).getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		amounts.setOriginalTotal(originalTotal);

		// 路徑一：免費報名 (後台新增 / 現場報到) —— 全額折抵，不套用優惠碼也不跑組合優惠
		if (isFree) {
			Map<Long, BigDecimal> discountByEventId = new HashMap<>();
			eventIds.forEach(
					eventId -> discountByEventId.put(eventId, pricingRuleByEventId.get(eventId).getAmount()));

			amounts.setDiscountByEventId(discountByEventId);
			amounts.setTotalDiscount(originalTotal);
			amounts.setFinalPrice(BigDecimal.ZERO);
			amounts.setAppliedDiscounts(Collections.emptyList());
			return amounts;
		}

		// 路徑二：有優惠碼 —— 走優惠碼，完全不跑組合優惠
		if (StringUtils.isNotBlank(promoCode)) {
			PromoCodeResultBO promoResult = promoCodeHelper.resolve(promoCode, eventIds, pricingRuleByEventId);

			amounts.setPromoCode(promoResult.getPromoCode());
			amounts.setPromoCampaign(promoResult.getPromoCampaign());
			amounts.setDiscountByEventId(promoResult.getDiscountByEventId());
			amounts.setTotalDiscount(promoResult.getTotalDiscount());
			amounts.setFinalPrice(promoResult.getFinalPrice());
			amounts.setAppliedDiscounts(List.of(AppliedDiscountBO.ofPromoCode(
					promoResult.getPromoCode().getPromoCodeId(), promoResult.getPromoCampaign().getName(),
					promoResult.getPromoCode().getCode(), promoResult.getTotalDiscount())));
			return amounts;
		}

		// 路徑三：沒有優惠碼 —— 走既有的活動組合優惠
		CalculateResultBO calculateResult = calculateDiscountHelper.calculateFinalPrices(pricingRuleByEventId,
				eventIds);

		// 組合優惠的折扣算在訂單層級，未分攤到單場，所以各場折抵為 0
		Map<Long, BigDecimal> discountByEventId = new HashMap<>();
		eventIds.forEach(eventId -> discountByEventId.put(eventId, BigDecimal.ZERO));

		amounts.setDiscountByEventId(discountByEventId);
		amounts.setTotalDiscount(calculateResult.getTotalDiscount());
		amounts.setFinalPrice(calculateResult.getFinalPrice());
		amounts.setAppliedDiscounts(calculateResult.getAppliedDiscounts());

		return amounts;
	}

	/**
	 * 把計算結果組裝成回傳給前端的 VO<br>
	 * 試算與實際報名共用，確保兩邊顯示結構一致
	 *
	 * @param eventIds
	 * @param amounts
	 * @return
	 */
	private EventOrderVO toEventOrderVO(List<Long> eventIds, OrderAmountsBO amounts) {

		EventOrderVO eventOrderVO = new EventOrderVO();
		eventOrderVO.setOriginalTotal(amounts.getOriginalTotal());
		eventOrderVO.setTotalDiscount(amounts.getTotalDiscount());
		eventOrderVO.setFinalPrice(amounts.getFinalPrice());
		eventOrderVO.setAppliedDiscounts(amounts.getAppliedDiscounts());

		// 逐場明細，依報名順序
		for (Long eventId : eventIds) {
			eventOrderVO.getEventPrices()
					.add(EventPriceBO.of(eventId, amounts.getEventById().get(eventId).getTitle(),
							amounts.getPricingRuleByEventId().get(eventId).getAmount(),
							amounts.getDiscountByEventId().getOrDefault(eventId, BigDecimal.ZERO)));
		}

		return eventOrderVO;
	}

	/**
	 * 
	 * 團體報名活動事件<br>
	 * 一次僅可報名 **一個** 事件<br>
	 * 需帶上團體報名者的專屬code號 (memberId)<br>
	 * 只能使用團體優惠組合
	 * 
	 * @param memberCache
	 * @param dto
	 * @return
	 */
	public EventOrderVO groupRegistration(Member memberCache, @Valid GroupRegistrationDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

}
