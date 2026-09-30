package tw.com.conference.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
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
import tw.com.conference.helper.AttendeeAdmissionHelper;
import tw.com.conference.helper.CalculateDiscountHelper;
import tw.com.conference.helper.MessageHelper;
import tw.com.conference.pojo.BO.CalculateResultBO;
import tw.com.conference.pojo.BO.EventPriceBO;
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
	 * 個人報名活動<br>
	 * 可一次報名 **多個** 事件<br>
	 * 可套用優惠組合
	 *
	 * @param member
	 * @param eventIds
	 */
	public EventOrderVO individualRegistration(Member member, List<Long> eventIds) {
		return this.registerEvents(member, eventIds, false);
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

		// 如果沒有活動則返回
		if (eventIds == null || eventIds.isEmpty()) {
			return null;
		}

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
		}

		// 獲取整包的金額、折扣金額、折扣項目
		CalculateResultBO calculateFinalPrices = calculateDiscountHelper.calculateFinalPrices(pricingRuleByEventId,
				eventIds);

		// 免費訂單：全額折抵，實付 0
		if (isFree) {
			calculateFinalPrices.setTotalDiscount(calculateFinalPrices.getOriginalTotal());
			calculateFinalPrices.setFinalPrice(BigDecimal.ZERO);
		}

		// 實付 0 元 即視為付款完成
		boolean isPaid = calculateFinalPrices.getFinalPrice().compareTo(BigDecimal.ZERO) == 0;

		// 拿到這次報名的活動資料
		Map<Long, Event> eventById = new HashMap<>();
		for (Long eventId : eventIds) {
			eventById.put(eventId, eventService.get(eventId));
		}

		// 創建訂單
		AddOrderDTO addOrderDTO = new AddOrderDTO();
		addOrderDTO.setStatus(isPaid ? OrderStatusEnum.PAYMENT_SUCCESS : OrderStatusEnum.UNPAID);
		addOrderDTO.setMemberId(member.getMemberId());
		// 綠界付款頁只能顯示商品概要，用 # 分段
		addOrderDTO.setItemsSummary(
				eventById.values().stream().map(Event::getTitle).collect(Collectors.joining("#")));
		addOrderDTO.setOriginalTotalAmount(calculateFinalPrices.getOriginalTotal());
		addOrderDTO.setTotalDiscountAmount(calculateFinalPrices.getTotalDiscount());
		addOrderDTO.setTotalAmount(calculateFinalPrices.getFinalPrice());
		addOrderDTO.setAppliedDiscounts(calculateFinalPrices.getAppliedDiscounts());
		if (isFree) {
			addOrderDTO.setRemark(OrderConstants.REMARK_FREE_BY_ADMIN);
		}
		Long orderId = ordersService.addOrder(addOrderDTO);

		// 創建返回對象
		EventOrderVO eventOrderVO = new EventOrderVO();

		// 創建訂單細項
		for (Map.Entry<Long, PricingRule> entry : pricingRuleByEventId.entrySet()) {
			Long eventId = entry.getKey();
			PricingRule pricingRule = entry.getValue();
			Event event = eventById.get(eventId);

			// VO中塞進這次報名的Event
			eventOrderVO.getEventPrices().add(new EventPriceBO(event.getTitle(), pricingRule.getAmount()));

			AddOrderItemDTO addOrderItemDTO = new AddOrderItemDTO();
			addOrderItemDTO.setOrdersId(orderId);
			addOrderItemDTO.setEventId(eventId);
			addOrderItemDTO.setProductType(OrderConstants.PRODUCT_TYPE_EVENT);
			addOrderItemDTO.setProductName(event.getTitle());
			addOrderItemDTO.setQuantity(1);
			addOrderItemDTO.setUnitPrice(pricingRule.getAmount());
			// 免費訂單：單價照價格規則記錄，但全額折抵
			addOrderItemDTO.setDiscount(isFree ? pricingRule.getAmount() : BigDecimal.ZERO);
			addOrderItemDTO.setSubtotal(isFree ? BigDecimal.ZERO : pricingRule.getAmount());

			ordersItemService.addOrderItem(addOrderItemDTO);
		}

		// 將BO的值copy過去VO
		BeanUtils.copyProperties(calculateFinalPrices, eventOrderVO);

		// 報名剛剛選擇參加的活動
		if (isPaid) {
			attendeeEventService.batchCreatePaidRecords(member.getMemberId(), eventIds);
			// 已付清，成為與會者
			attendeeAdmissionHelper.admitAfterPayment(member);
		} else {
			attendeeEventService.batchCreateUnpaidRecords(member.getMemberId(), eventIds);
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
