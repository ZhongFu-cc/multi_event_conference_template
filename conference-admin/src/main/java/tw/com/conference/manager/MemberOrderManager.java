package tw.com.conference.manager;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.MemberConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.pojo.BO.MemberExcelRaw;
import tw.com.conference.pojo.DTO.OfflineTransferDTO;
import tw.com.conference.pojo.VO.EventUnpaidMemberVO;
import tw.com.conference.pojo.VO.MemberOrderVO;
import tw.com.conference.pojo.VO.MemberVO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.pojo.entity.Orders;
import tw.com.conference.pojo.entity.OrdersItem;
import tw.com.conference.pojo.excelPojo.MemberExcel;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.EventService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.service.OrdersItemService;
import tw.com.conference.service.OrdersService;

/**
 * 管理會員 和 訂單的需求<br>
 * 所有「繳費狀態」皆以 活動(eventId) 為單位，透過 attendee_event 判斷
 */
@Component
@RequiredArgsConstructor
public class MemberOrderManager {

	private final MemberConvert memberConvert;
	private final MemberService memberService;
	private final MemberTypeService memberTypeService;
	private final EventService eventService;
	private final AttendeeEventService attendeeEventService;
	private final OrdersService ordersService;
	private final OrdersItemService ordersItemService;

	// --------------------------- 查詢相關 ---------------------------------------

	/**
	 * 拿到帶有各活動繳費狀態的VO對象 (會員本人查看)
	 *
	 * @param memberId
	 * @return
	 */
	public MemberVO getMemberVO(Long memberId) {
		Member member = memberService.getMember(memberId);
		MemberVO vo = memberConvert.entityToVO(member);
		vo.setEventStatusList(attendeeEventService.findEventStatusByMemberId(memberId));
		return vo;
	}

	/**
	 * 獲得 報名某活動 且 符合繳費狀態 的會員人數
	 *
	 * @param eventId
	 * @param isPaid  null 為不限
	 * @return
	 */
	public Integer getMemberCountByEvent(Long eventId, CommonStatusEnum isPaid) {
		return attendeeEventService.findMemberIdsByEventAndPaid(eventId, isPaid).size();
	}

	/**
	 * 獲得 報名某活動 且 符合繳費狀態 的會員及其(含此活動的)訂單 VO對象
	 *
	 * @param page
	 * @param eventId
	 * @param isPaid    null 為不限
	 * @param queryText
	 * @return
	 */
	public IPage<MemberOrderVO> getMemberOrderVO(Page<Member> page, Long eventId, CommonStatusEnum isPaid,
			String queryText) {

		// 1.找出報名此活動且符合繳費狀態的會員
		List<Long> memberIds = attendeeEventService.findMemberIdsByEventAndPaid(eventId, isPaid);
		if (memberIds.isEmpty()) {
			return new Page<>(page.getCurrent(), page.getSize());
		}

		// 2.分頁查詢會員
		IPage<Member> memberPage = memberService.getMemberPageByQuery(page, queryText, memberIds);
		if (memberPage.getRecords().isEmpty()) {
			return new Page<>(page.getCurrent(), page.getSize(), memberPage.getTotal());
		}

		// 3.找出這頁會員 含此活動 的訂單
		Set<Long> pageMemberIds = memberPage.getRecords().stream().map(Member::getMemberId)
				.collect(Collectors.toSet());
		Map<Long, List<Orders>> ordersByMemberId = this.findOrdersContainingEvent(eventId, pageMemberIds);

		// 4.組裝VO
		List<MemberOrderVO> voList = memberPage.getRecords().stream().map(member -> {
			MemberOrderVO vo = memberConvert.entityToMemberOrderVO(member);
			vo.setOrdersList(ordersByMemberId.getOrDefault(member.getMemberId(), Collections.emptyList()));
			return vo;
		}).toList();

		IPage<MemberOrderVO> resultPage = new Page<>(memberPage.getCurrent(), memberPage.getSize(),
				memberPage.getTotal());
		resultPage.setRecords(voList);
		return resultPage;
	}

	/**
	 * 後台審核用：列出 報名某活動但尚未繳費 的會員，附上包含此活動的未付訂單
	 *
	 * @param page
	 * @param eventId
	 * @param queryText
	 * @return
	 */
	public IPage<EventUnpaidMemberVO> getUnpaidMembersByEvent(Page<Member> page, Long eventId, String queryText) {

		// 1.找出報名此活動且未繳費的會員
		List<Long> memberIds = attendeeEventService.findMemberIdsByEventAndPaid(eventId, CommonStatusEnum.NO);
		if (memberIds.isEmpty()) {
			return new Page<>(page.getCurrent(), page.getSize());
		}

		// 2.分頁查詢會員
		IPage<Member> memberPage = memberService.getMemberPageByQuery(page, queryText, memberIds);
		if (memberPage.getRecords().isEmpty()) {
			return new Page<>(page.getCurrent(), page.getSize(), memberPage.getTotal());
		}

		// 3.找出這頁會員 含此活動 且 尚未付款成功 的訂單
		Set<Long> pageMemberIds = memberPage.getRecords().stream().map(Member::getMemberId)
				.collect(Collectors.toSet());
		List<Orders> unpaidOrders = ordersService.findOrdersByMemberIdsAndStatus(pageMemberIds,
				List.of(OrderStatusEnum.UNPAID, OrderStatusEnum.PENDING_CONFIRMATION));
		Map<Long, List<OrdersItem>> itemsByOrderId = ordersItemService
				.findOrderItemsByOrderIds(unpaidOrders.stream().map(Orders::getOrdersId).toList())
				.stream()
				.collect(Collectors.groupingBy(OrdersItem::getOrdersId));

		// 一個會員對同一活動只會有一張訂單 (registerEvents 已擋重複報名)，直接以 memberId 為 key
		Map<Long, Orders> orderByMemberId = unpaidOrders.stream()
				.filter(order -> itemsByOrderId.getOrDefault(order.getOrdersId(), Collections.emptyList())
						.stream()
						.anyMatch(item -> eventId.equals(item.getEventId())))
				.collect(Collectors.toMap(Orders::getMemberId, Function.identity()));

		// 4.組裝VO
		List<EventUnpaidMemberVO> voList = memberPage.getRecords().stream().map(member -> {
			EventUnpaidMemberVO vo = new EventUnpaidMemberVO();
			vo.setMemberId(member.getMemberId());
			vo.setChineseName(member.getChineseName());
			vo.setFirstName(member.getFirstName());
			vo.setLastName(member.getLastName());
			vo.setEmail(member.getEmail());
			vo.setPhone(member.getPhone());
			vo.setCountry(member.getCountry());
			vo.setRemitAccountLast5(member.getRemitAccountLast5());

			Orders order = orderByMemberId.get(member.getMemberId());
			if (order != null) {
				vo.setOrdersId(order.getOrdersId());
				vo.setOrderStatus(order.getStatus());
				vo.setOriginalTotalAmount(order.getOriginalTotalAmount());
				vo.setTotalDiscountAmount(order.getTotalDiscountAmount());
				vo.setTotalAmount(order.getTotalAmount());
				vo.setAppliedDiscounts(order.getAppliedDiscounts());
				vo.setOrderEventTitles(itemsByOrderId.getOrDefault(order.getOrdersId(), Collections.emptyList())
						.stream()
						.map(OrdersItem::getProductName)
						.toList());
			}
			return vo;
		}).toList();

		IPage<EventUnpaidMemberVO> resultPage = new Page<>(memberPage.getCurrent(), memberPage.getSize(),
				memberPage.getTotal());
		resultPage.setRecords(voList);
		return resultPage;
	}

	/**
	 * 找出指定會員中，訂單細項包含某活動的訂單
	 *
	 * @param eventId
	 * @param memberIds
	 * @return memberId -> 訂單列表
	 */
	private Map<Long, List<Orders>> findOrdersContainingEvent(Long eventId, Set<Long> memberIds) {
		// 這些會員的所有訂單
		List<Orders> orders = ordersService.findOrdersByMemberIdsAndStatus(memberIds, null);
		if (orders.isEmpty()) {
			return Collections.emptyMap();
		}
		// 只留下細項含此活動的訂單
		Set<Long> orderIdsWithEvent = ordersItemService
				.findOrderItemsByOrderIds(orders.stream().map(Orders::getOrdersId).toList())
				.stream()
				.filter(item -> eventId.equals(item.getEventId()))
				.map(OrdersItem::getOrdersId)
				.collect(Collectors.toSet());

		return orders.stream()
				.filter(order -> orderIdsWithEvent.contains(order.getOrdersId()))
				.collect(Collectors.groupingBy(Orders::getMemberId));
	}

	/**
	 * 離線/人工 匯款<br>
	 * 使用者送出確認，等待管理員審核
	 *
	 * @param offlineTransferDTO
	 */
	public void offlineTransfer(OfflineTransferDTO offlineTransferDTO) {
		Orders order = ordersService.getOrders(offlineTransferDTO.getOrderId());
		Member member = memberService.getMember(order.getMemberId());

		// 修改會員卡號末五碼，不管新舊，理論上就是以這個為準
		member.setRemitAccountLast5(offlineTransferDTO.getRemitAccountLast5());
		memberService.updateById(member);

		// 不管狀態為何,觸發則將訂單狀態改為 付款-待確認
		order.setStatus(OrderStatusEnum.PENDING_CONFIRMATION);
		ordersService.updateById(order);
	}

	/**
	 * 下載 報名某活動 的會員列表, 其中包含他們對此活動的付款狀態與費用
	 *
	 * @param response
	 * @param eventId
	 * @throws IOException
	 */
	public void downloadExcel(HttpServletResponse response, Long eventId) throws IOException {

		Event event = eventService.get(eventId);

		// 1.設置Excel 檔案資訊
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		// 这里URLEncoder.encode可以防止中文乱码 ， 和easyexcel没有关系
		String fileName = URLEncoder.encode("會員名單-" + event.getTitle(), "UTF-8").replaceAll("\\+", "%20");
		response.setHeader("Content-disposition", "attachment;filename*=" + fileName + ".xlsx");

		// 2.報名此活動的會員 及 其繳費狀態
		List<Long> memberIds = attendeeEventService.findMemberIdsByEventAndPaid(eventId, null);
		Map<Long, CommonStatusEnum> paidMapByMemberId = attendeeEventService.getPaidMapByEventAndMemberIds(eventId,
				memberIds);

		// 3.此活動的訂單細項 (取該會員此活動的實付小計)
		Map<Long, BigDecimal> subtotalByMemberId = this.findEventSubtotalByMemberId(eventId, memberIds);

		// 4.會員類別名稱
		Map<Long, String> memberTypeLabelById = memberTypeService.list()
				.stream()
				.collect(Collectors.toMap(MemberType::getMemberTypeId, MemberType::getLabelZh, (a, b) -> a));

		// 5.遍歷會員資料,組裝excelVO對象
		List<Member> memberList = memberIds.isEmpty() ? Collections.emptyList()
				: memberService.getMemberPageByQuery(new Page<>(1, Long.MAX_VALUE), null, memberIds).getRecords();

		List<MemberExcel> excelData = memberList.stream().map(member -> {
			MemberExcelRaw memberExcelRaw = memberConvert.entityToExcelRaw(member);
			boolean isPaid = CommonStatusEnum.YES.equals(paidMapByMemberId.get(member.getMemberId()));
			memberExcelRaw.setStatus(
					isPaid ? OrderStatusEnum.PAYMENT_SUCCESS.getLabelZh() : OrderStatusEnum.UNPAID.getLabelZh());
			memberExcelRaw.setRegistrationFee(subtotalByMemberId.getOrDefault(member.getMemberId(), BigDecimal.ZERO));
			memberExcelRaw.setMemberType(memberTypeLabelById.get(member.getMemberTypeId()));
			return memberConvert.memberExcelRawToExcel(memberExcelRaw);
		}).toList();

		// 6.輸出成Excel
		EasyExcel.write(response.getOutputStream(), MemberExcel.class).sheet("會員列表").doWrite(excelData);
	}

	/**
	 * 找出各會員對某活動的訂單細項小計 (一個會員對同一活動只會有一筆細項)
	 */
	private Map<Long, BigDecimal> findEventSubtotalByMemberId(Long eventId, List<Long> memberIds) {
		List<OrdersItem> items = ordersItemService.findOrderItemsByEventId(eventId);
		if (items.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Long, Orders> orderById = ordersService
				.listByIds(items.stream().map(OrdersItem::getOrdersId).distinct().toList())
				.stream()
				.collect(Collectors.toMap(Orders::getOrdersId, Function.identity()));

		return items.stream()
				.filter(item -> orderById.containsKey(item.getOrdersId()))
				.collect(Collectors.toMap(item -> orderById.get(item.getOrdersId()).getMemberId(),
						OrdersItem::getSubtotal));
	}

}
