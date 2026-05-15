package tw.com.conference.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.helper.CalculateDiscountHelper;
import tw.com.conference.pojo.BO.CalculateResultBO;
import tw.com.conference.pojo.BO.EventPriceBO;
import tw.com.conference.pojo.DTO.GroupRegistrationDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderItemDTO;
import tw.com.conference.pojo.VO.EventOrderVO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.pojo.entity.PricingRule;
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
	private final OrdersService ordersService;
	private final OrdersItemService ordersItemService;
	private final MemberTypeService memberTypeService;
	private final PricingRuleService pricingRuleService;
	private final CalculateDiscountHelper calculateDiscountHelper;

	/**
	 * 獲取當下可報名的Event
	 */
	public void findAvailableEvent() {

		// 1.先拿到當下時間，符合報名時段 及 啟用中兩個條件的活動
		List<Event> availableEvent = eventService.findAvailable();

		// 2.將活動ID 再去與 attendEvent 表去做人數的比對,確定沒有達到限制人數

		// 

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

		// 如果沒有活動則返回
		if (eventIds != null && eventIds.isEmpty()) {
			return null;
		}

		// 先獲取會員身分
		MemberType memberType = memberTypeService.get(member.getMemberTypeId());
		// 獲取會員國籍
		NationalityEnum nationalityEnum = CountryUtil.getDomesticOrInternational(member.getCountry());
		// 獲取當前時間
		LocalDateTime now = LocalDateTime.now();

		// 初始化一個以eventId為key,PricingRule為值的Map對象,
		Map<Long, PricingRule> pricingRuleByEventId = new HashMap<>();

		// 遍歷他所報名的活動,搭配會員的身分及報名時間,得到當前匹配的價格規則 (該場活動要付的錢)
		for (Long eventId : eventIds) {
			// 找到當前匹配的價格規則
			PricingRule pricingRule = pricingRuleService.resolvePricingRule(eventId, memberType.getMemberTypeId(),
					nationalityEnum, now);
			pricingRuleByEventId.put(eventId, pricingRule);
		}

		// 獲取整包的金額、折扣金額、折扣項目
		CalculateResultBO calculateFinalPrices = calculateDiscountHelper.calculateFinalPrices(pricingRuleByEventId,
				eventIds);

		AddOrderDTO addOrderDTO = new AddOrderDTO();
		// 創建訂單
		if (calculateFinalPrices.getFinalPrice().equals(BigDecimal.ZERO)) {
			addOrderDTO.setStatus(OrderStatusEnum.PAYMENT_SUCCESS);
		} else {
			addOrderDTO.setStatus(OrderStatusEnum.UNPAID);
		}
		addOrderDTO.setMemberId(member.getMemberId());
		addOrderDTO.setOriginalTotalAmount(calculateFinalPrices.getOriginalTotal());
		addOrderDTO.setTotalDiscountAmount(calculateFinalPrices.getOriginalTotal());
		addOrderDTO.setTotalAmount(calculateFinalPrices.getFinalPrice());
		addOrderDTO.setAppliedDiscounts(calculateFinalPrices.getAppliedDiscounts());
		Long orderId = ordersService.addOrder(addOrderDTO);

		// 創建返回對象
		EventOrderVO eventOrderVO = new EventOrderVO();

		// 創建訂單細項
		for (Map.Entry<Long, PricingRule> entry : pricingRuleByEventId.entrySet()) {
			Long eventId = entry.getKey();
			PricingRule pricingRule = entry.getValue();
			Event event = eventService.get(eventId);

			// VO中塞進這次報名的Event
			System.out.println("活動:" + event.getTitle() + " " + "金額" + pricingRule.getAmount());

			eventOrderVO.getEventPrices().add(new EventPriceBO(event.getTitle(), pricingRule.getAmount()));

			AddOrderItemDTO addOrderItemDTO = new AddOrderItemDTO();
			addOrderItemDTO.setOrdersId(orderId);
			addOrderItemDTO.setProductType("event");
			addOrderItemDTO.setProductName(event.getTitle());
			addOrderItemDTO.setQuantity(1);
			addOrderItemDTO.setUnitPrice(pricingRule.getAmount());
			addOrderItemDTO.setSubtotal(pricingRule.getAmount());

			ordersItemService.addOrderItem(addOrderItemDTO);
		}

		// 將BO的值copy過去VO
		BeanUtils.copyProperties(calculateFinalPrices, eventOrderVO);

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
