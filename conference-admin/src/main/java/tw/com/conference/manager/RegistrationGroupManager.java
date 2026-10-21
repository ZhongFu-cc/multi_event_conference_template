package tw.com.conference.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import tw.com.conference.constants.I18nMessageKey;
import tw.com.conference.constants.OrderConstants;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.enums.RegistrationGroupRoleEnum;
import tw.com.conference.enums.RegistrationGroupStatusEnum;
import tw.com.conference.exception.EventException;
import tw.com.conference.exception.RegistrationClosedException;
import tw.com.conference.exception.RegistrationGroupException;
import tw.com.conference.helper.AttendeeAdmissionHelper;
import tw.com.conference.helper.MessageHelper;
import tw.com.conference.pojo.BO.AppliedDiscountBO;
import tw.com.conference.pojo.BO.GroupMemberPriceBO;
import tw.com.conference.pojo.DTO.JoinRegistrationGroupDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddOrderItemDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddRegistrationGroupDTO;
import tw.com.conference.pojo.VO.GroupInvitePreviewVO;
import tw.com.conference.pojo.VO.GroupOrderVO;
import tw.com.conference.pojo.VO.RegistrationGroupMemberVO;
import tw.com.conference.pojo.VO.RegistrationGroupVO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.GroupDiscountRule;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.pojo.entity.Orders;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.pojo.entity.RegistrationGroup;
import tw.com.conference.pojo.entity.RegistrationGroupMember;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.EventService;
import tw.com.conference.service.GroupDiscountRuleService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.service.OrdersItemService;
import tw.com.conference.service.OrdersService;
import tw.com.conference.service.PricingRuleService;
import tw.com.conference.service.RegistrationGroupMemberService;
import tw.com.conference.service.RegistrationGroupService;
import tw.com.conference.service.SettingService;
import tw.com.conference.utils.CountryUtil;

/**
 * 團體報名 (邀請碼機制)<br>
 * <br>
 * 流程：負責人建立團體取得邀請碼 → 成員以自己的帳號輸入邀請碼加入 → 人數達門檻後負責人鎖定名單
 * → 建立一張由負責人付款的訂單 → 付款成功後所有成員一起完成報名<br>
 * <br>
 * 規則：<br>
 * 1. 一團只報名一場活動；要報名另一場需另建一團<br>
 * 2. 負責人一定出席且計入人數<br>
 * 3. 人數未達最低門檻不可鎖定 (前端也需擋)<br>
 * 4. 團體優惠與優惠碼、活動組合優惠互斥 —— 團體訂單只會套用團體優惠<br>
 * 5. 每位成員的價格依「鎖定當下」的會員身分與國籍計算，成員可在加入後自行更新身分<br>
 * <br>
 * 會改動團體成員或狀態的操作都以 Redisson 鎖住該團體，避免「鎖定」與「加入」同時發生；
 * 鎖在交易提交後才釋放，所以交易以 TransactionTemplate 包在鎖內。
 */
@Component
@RequiredArgsConstructor
public class RegistrationGroupManager {

	private static final String GROUP_LOCK_PREFIX = "registration-group:group:";
	private static final String MEMBER_LOCK_PREFIX = "registration-group:member:";
	private static final long LOCK_LEASE_SECONDS = 30;
	private static final BigDecimal HUNDRED = new BigDecimal(100);

	private final MessageHelper messageHelper;
	private final RedissonClient redissonClient;
	private final TransactionTemplate transactionTemplate;
	private final AttendeeAdmissionHelper attendeeAdmissionHelper;
	private final RegistrationGroupService registrationGroupService;
	private final RegistrationGroupMemberService registrationGroupMemberService;
	private final GroupDiscountRuleService groupDiscountRuleService;
	private final EventService eventService;
	private final MemberService memberService;
	private final MemberTypeService memberTypeService;
	private final PricingRuleService pricingRuleService;
	private final AttendeeEventService attendeeEventService;
	private final OrdersService ordersService;
	private final OrdersItemService ordersItemService;
	private final SettingService settingService;

	/**
	 * 查詢某場活動啟用中的團體優惠規則 (給會員看優惠內容)
	 *
	 * @param eventId
	 * @return
	 */
	public List<GroupDiscountRule> findActiveRules(Long eventId) {
		return groupDiscountRuleService.findActiveByEventId(eventId);
	}

	/**
	 * 建立團體，建立者成為負責人並自動加入
	 *
	 * @param leader
	 * @param dto
	 * @return
	 */
	public RegistrationGroupVO createGroup(Member leader, AddRegistrationGroupDTO dto) {

		Event event = this.validateEventForGroup(dto.getEventId());

		return this.withLocks(List.of(this.memberLockKey(leader.getMemberId(), event.getEventId())), () -> {

			this.validateMemberCanJoin(leader.getMemberId(), event);

			// 團體名稱由系統產生，不開放負責人自訂; 以 DB 最新資料為準 (session 內的會員資料可能過舊)
			String groupName = this.generateGroupName(memberService.getMember(leader.getMemberId()), event);

			RegistrationGroup registrationGroup = registrationGroupService.create(event.getEventId(),
					leader.getMemberId(), groupName);
			registrationGroupMemberService.addMember(registrationGroup.getRegistrationGroupId(),
					leader.getMemberId(), RegistrationGroupRoleEnum.LEADER);

			return this.toVO(registrationGroup, leader.getMemberId(), false);
		});
	}

	/**
	 * 查詢會員參與的所有團體 (擔任負責人或成員)
	 *
	 * @param member
	 * @return
	 */
	public List<RegistrationGroupVO> findMyGroups(Member member) {

		List<Long> groupIds = registrationGroupMemberService.findByMemberId(member.getMemberId())
				.stream()
				.map(RegistrationGroupMember::getRegistrationGroupId)
				.toList();

		return registrationGroupService.findByIds(groupIds)
				.stream()
				.map(registrationGroup -> this.toVO(registrationGroup, member.getMemberId(), false))
				.toList();
	}

	/**
	 * 查詢單一團體 (必須是該團成員)
	 *
	 * @param member
	 * @param registrationGroupId
	 * @return
	 */
	public RegistrationGroupVO getGroup(Member member, Long registrationGroupId) {
		RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
		this.requireMembership(registrationGroup, member.getMemberId());
		return this.toVO(registrationGroup, member.getMemberId(), false);
	}

	/**
	 * 後台查詢某場活動的所有團體及完整成員名單
	 *
	 * @param eventId
	 * @return
	 */
	public List<RegistrationGroupVO> findGroupsByEvent(Long eventId) {
		return registrationGroupService.findByEventId(eventId)
				.stream()
				.map(registrationGroup -> this.toVO(registrationGroup, null, true))
				.toList();
	}

	/**
	 * 以邀請碼查看團體概況 (確認加入前)
	 *
	 * @param member
	 * @param inviteCode
	 * @return
	 */
	public GroupInvitePreviewVO previewInvite(Member member, String inviteCode) {

		RegistrationGroup registrationGroup = this.requireGroupByInviteCode(inviteCode);
		Event event = eventService.get(registrationGroup.getEventId());
		Member leader = memberService.getMember(registrationGroup.getLeaderMemberId());

		GroupInvitePreviewVO vo = new GroupInvitePreviewVO();
		vo.setRegistrationGroupId(registrationGroup.getRegistrationGroupId());
		vo.setGroupName(registrationGroup.getName());
		vo.setEventId(registrationGroup.getEventId());
		vo.setEventTitle(event == null ? null : event.getTitle());
		vo.setLeaderName(this.displayName(leader));
		vo.setMemberCount((int) registrationGroupMemberService.countByGroupId(registrationGroup.getRegistrationGroupId()));
		vo.setStatus(registrationGroup.getStatus());
		vo.setAlreadyJoined(registrationGroupMemberService
				.getByGroupAndMember(registrationGroup.getRegistrationGroupId(), member.getMemberId()) != null);
		return vo;
	}

	/**
	 * 以邀請碼加入團體
	 *
	 * @param member
	 * @param dto
	 * @return
	 */
	public RegistrationGroupVO joinGroup(Member member, JoinRegistrationGroupDTO dto) {

		RegistrationGroup found = this.requireGroupByInviteCode(dto.getInviteCode());

		// 鎖順序固定為「團體 → 會員」，與其他操作一致以避免死鎖
		List<String> lockKeys = List.of(this.groupLockKey(found.getRegistrationGroupId()),
				this.memberLockKey(member.getMemberId(), found.getEventId()));

		return this.withLocks(lockKeys, () -> {

			// 取得鎖後重新讀取，確保狀態是最新的
			RegistrationGroup registrationGroup = this.requireGroup(found.getRegistrationGroupId());
			this.requireRecruiting(registrationGroup);

			Event event = this.validateEventForGroup(registrationGroup.getEventId());

			if (registrationGroupMemberService.getByGroupAndMember(registrationGroup.getRegistrationGroupId(),
					member.getMemberId()) != null) {
				throw new RegistrationGroupException(
						messageHelper.get(I18nMessageKey.Registration.Group.ALREADY_JOINED));
			}

			this.validateMemberCanJoin(member.getMemberId(), event);

			registrationGroupMemberService.addMember(registrationGroup.getRegistrationGroupId(),
					member.getMemberId(), RegistrationGroupRoleEnum.MEMBER);

			return this.toVO(registrationGroup, member.getMemberId(), false);
		});
	}

	/**
	 * 成員自行退出團體 (僅招募中)
	 *
	 * @param member
	 * @param registrationGroupId
	 */
	public void leaveGroup(Member member, Long registrationGroupId) {
		this.runWithLocks(List.of(this.groupLockKey(registrationGroupId)), () -> {

			RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
			RegistrationGroupMember groupMember = this.requireMembership(registrationGroup, member.getMemberId());

			if (RegistrationGroupRoleEnum.LEADER.equals(groupMember.getRole())) {
				throw new RegistrationGroupException(
						messageHelper.get(I18nMessageKey.Registration.Group.LEADER_CANNOT_LEAVE));
			}

			this.requireRecruiting(registrationGroup);

			registrationGroupMemberService.remove(groupMember.getRegistrationGroupMemberId());
		});
	}

	/**
	 * 負責人移除成員 (僅招募中)
	 *
	 * @param leader
	 * @param registrationGroupId
	 * @param memberId            要移除的成員
	 */
	public void removeMember(Member leader, Long registrationGroupId, Long memberId) {
		this.runWithLocks(List.of(this.groupLockKey(registrationGroupId)), () -> {

			RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
			this.requireLeader(registrationGroup, leader.getMemberId());
			this.requireRecruiting(registrationGroup);

			RegistrationGroupMember target = registrationGroupMemberService.getByGroupAndMember(registrationGroupId,
					memberId);
			if (target == null) {
				throw new RegistrationGroupException(
						messageHelper.get(I18nMessageKey.Registration.Group.MEMBER_NOT_FOUND));
			}
			if (RegistrationGroupRoleEnum.LEADER.equals(target.getRole())) {
				throw new RegistrationGroupException(
						messageHelper.get(I18nMessageKey.Registration.Group.LEADER_CANNOT_LEAVE));
			}

			registrationGroupMemberService.remove(target.getRegistrationGroupMemberId());
		});
	}

	/**
	 * 負責人重新產生邀請碼，舊碼立即失效 (僅招募中)
	 *
	 * @param leader
	 * @param registrationGroupId
	 * @return 新的邀請碼
	 */
	public String regenerateInviteCode(Member leader, Long registrationGroupId) {
		return this.withLocks(List.of(this.groupLockKey(registrationGroupId)), () -> {

			RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
			this.requireLeader(registrationGroup, leader.getMemberId());
			this.requireRecruiting(registrationGroup);

			return registrationGroupService.regenerateInviteCode(registrationGroup);
		});
	}

	/**
	 * 負責人解散團體 (僅招募中; 已鎖定需先解除鎖定)
	 *
	 * @param leader
	 * @param registrationGroupId
	 */
	public void dissolveGroup(Member leader, Long registrationGroupId) {
		this.runWithLocks(List.of(this.groupLockKey(registrationGroupId)), () -> {

			RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
			this.requireLeader(registrationGroup, leader.getMemberId());
			this.requireRecruiting(registrationGroup);

			registrationGroupMemberService.removeByGroupId(registrationGroupId);
			registrationGroupService.remove(registrationGroupId);
		});
	}

	/**
	 * 負責人試算團體訂單金額 (不寫入任何資料)<br>
	 * 未達門檻時不拋錯，回傳 eligible=false 讓前端顯示還差幾人
	 *
	 * @param leader
	 * @param registrationGroupId
	 * @return
	 */
	public GroupOrderVO previewOrder(Member leader, Long registrationGroupId) {

		RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
		this.requireLeader(registrationGroup, leader.getMemberId());

		return this.calculateOrder(registrationGroup,
				registrationGroupMemberService.findByGroupId(registrationGroupId));
	}

	/**
	 * 負責人鎖定名單並建立團體訂單<br>
	 * 鎖定後成員不可再加入 / 退出；訂單由負責人付款，付款成功後所有成員一起完成報名
	 *
	 * @param leader
	 * @param registrationGroupId
	 * @return
	 */
	public GroupOrderVO lockGroup(Member leader, Long registrationGroupId) {
		return this.withLocks(List.of(this.groupLockKey(registrationGroupId)), () -> {

			// 1.校驗身分、狀態與活動
			RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
			this.requireLeader(registrationGroup, leader.getMemberId());
			this.requireRecruiting(registrationGroup);
			Event event = this.validateEventForGroup(registrationGroup.getEventId());

			// 2.計算金額; 未達人數門檻直接擋下
			List<RegistrationGroupMember> groupMembers = registrationGroupMemberService
					.findByGroupId(registrationGroupId);
			GroupOrderVO groupOrder = this.calculateOrder(registrationGroup, groupMembers);
			if (!groupOrder.getEligible()) {
				throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.BELOW_MIN_COUNT,
						groupOrder.getMinCount(), groupOrder.getMemberCount()));
			}

			boolean paidOff = groupOrder.getFinalPrice().compareTo(BigDecimal.ZERO) == 0;

			// 3.建立訂單，持有者為負責人
			AddOrderDTO addOrderDTO = new AddOrderDTO();
			addOrderDTO.setStatus(paidOff ? OrderStatusEnum.PAYMENT_SUCCESS : OrderStatusEnum.UNPAID);
			addOrderDTO.setMemberId(registrationGroup.getLeaderMemberId());
			addOrderDTO.setItemsSummary(event.getTitle() + " x " + groupOrder.getMemberCount());
			addOrderDTO.setOriginalTotalAmount(groupOrder.getOriginalTotal());
			addOrderDTO.setTotalDiscountAmount(groupOrder.getTotalDiscount());
			addOrderDTO.setTotalAmount(groupOrder.getFinalPrice());
			addOrderDTO.setAppliedDiscounts(groupOrder.getAppliedDiscounts());
			Long ordersId = ordersService.addOrder(addOrderDTO);

			// 4.建立訂單細項，每位成員一筆
			// 單價記原價，折抵記該成員分到的團體折扣，小計為該成員實付
			for (GroupMemberPriceBO memberPrice : groupOrder.getMemberPrices()) {
				AddOrderItemDTO addOrderItemDTO = new AddOrderItemDTO();
				addOrderItemDTO.setOrdersId(ordersId);
				addOrderItemDTO.setEventId(event.getEventId());
				addOrderItemDTO.setMemberId(memberPrice.getMemberId());
				addOrderItemDTO.setProductType(OrderConstants.PRODUCT_TYPE_EVENT);
				addOrderItemDTO.setProductName(event.getTitle() + " - " + memberPrice.getMemberName());
				addOrderItemDTO.setQuantity(1);
				addOrderItemDTO.setUnitPrice(memberPrice.getOriginalPrice());
				addOrderItemDTO.setDiscount(memberPrice.getDiscount());
				addOrderItemDTO.setSubtotal(memberPrice.getPayableAmount());
				ordersItemService.addOrderItem(addOrderItemDTO);
			}

			// 5.團體狀態轉換並記下訂單
			RegistrationGroupStatusEnum nextStatus = paidOff ? RegistrationGroupStatusEnum.PAID
					: RegistrationGroupStatusEnum.LOCKED;
			if (!registrationGroupService.transitStatus(registrationGroupId, RegistrationGroupStatusEnum.RECRUITING,
					nextStatus, ordersId)) {
				throw new RegistrationGroupException(
						messageHelper.get(I18nMessageKey.Registration.Group.STATUS_CHANGED));
			}

			// 6.為每位成員建立報名紀錄
			Set<Long> eventIds = Set.of(event.getEventId());
			if (paidOff) {
				for (GroupMemberPriceBO memberPrice : groupOrder.getMemberPrices()) {
					attendeeEventService.batchCreatePaidRecords(memberPrice.getMemberId(), eventIds);
					attendeeAdmissionHelper.admitAfterPayment(memberService.getMember(memberPrice.getMemberId()));
				}
			} else {
				for (GroupMemberPriceBO memberPrice : groupOrder.getMemberPrices()) {
					attendeeEventService.batchCreateUnpaidRecords(memberPrice.getMemberId(), eventIds);
				}
				// 只有負責人持有未付訂單，未付款標籤只掛在負責人身上
				attendeeAdmissionHelper.markUnpaid(registrationGroup.getLeaderMemberId());
			}

			groupOrder.setOrdersId(ordersId);
			return groupOrder;
		});
	}

	/**
	 * 負責人解除鎖定 (僅已鎖定且未付款)<br>
	 * 刪除團體訂單，團體退回招募中 (退回邏輯在 OrdersService.deleteOrders 內統一處理)
	 *
	 * @param leader
	 * @param registrationGroupId
	 */
	public void unlockGroup(Member leader, Long registrationGroupId) {
		this.runWithLocks(List.of(this.groupLockKey(registrationGroupId)), () -> {

			RegistrationGroup registrationGroup = this.requireGroup(registrationGroupId);
			this.requireLeader(registrationGroup, leader.getMemberId());

			if (RegistrationGroupStatusEnum.PAID.equals(registrationGroup.getStatus())) {
				throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.ALREADY_PAID));
			}
			if (!RegistrationGroupStatusEnum.LOCKED.equals(registrationGroup.getStatus())) {
				throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.NOT_LOCKED));
			}

			// 已付款-待審核 / 付款成功 的訂單不可解除，避免款項已入帳卻刪單
			Orders order = ordersService.getOrders(registrationGroup.getOrdersId());
			if (order != null && !OrderStatusEnum.UNPAID.equals(order.getStatus())
					&& !OrderStatusEnum.PAYMENT_FAILED.equals(order.getStatus())) {
				throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.ALREADY_PAID));
			}

			ordersService.deleteOrders(registrationGroup.getOrdersId());
		});
	}

	/**
	 * 計算團體訂單金額 (純計算)
	 *
	 * @param registrationGroup
	 * @param groupMembers
	 * @return
	 */
	private GroupOrderVO calculateOrder(RegistrationGroup registrationGroup,
			List<RegistrationGroupMember> groupMembers) {

		Long eventId = registrationGroup.getEventId();
		LocalDateTime now = LocalDateTime.now();

		// 1.重新從 DB 讀取成員資料，身分可能在加入後有變動
		Map<Long, Member> memberById = this.loadMembers(groupMembers);
		Map<Long, MemberType> memberTypeById = new HashMap<>();

		// 2.逐人計算原價
		List<GroupMemberPriceBO> memberPrices = new ArrayList<>();
		for (RegistrationGroupMember groupMember : groupMembers) {
			Member member = memberById.get(groupMember.getMemberId());

			// 加入後才自行個人報名的成員要先移除，否則會重複報名
			if (attendeeEventService.getByMemberAndEvent(member.getMemberId(), eventId) != null) {
				throw new RegistrationGroupException(messageHelper.get(
						I18nMessageKey.Registration.Group.MEMBER_ALREADY_REGISTERED, (Object) this.displayName(member)));
			}

			MemberType memberType = memberTypeById.computeIfAbsent(member.getMemberTypeId(), memberTypeService::get);
			PricingRule pricingRule = pricingRuleService.resolvePricingRule(eventId, memberType.getMemberTypeId(),
					CountryUtil.getDomesticOrInternational(member.getCountry()), now);

			GroupMemberPriceBO memberPrice = new GroupMemberPriceBO();
			memberPrice.setMemberId(member.getMemberId());
			memberPrice.setMemberName(this.displayName(member));
			memberPrice.setMemberTypeLabel(memberType.getLabelZh());
			memberPrice.setOriginalPrice(pricingRule.getAmount());
			memberPrices.add(memberPrice);
		}

		BigDecimal originalTotal = memberPrices.stream()
				.map(GroupMemberPriceBO::getOriginalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		// 3.依人數找出可套用的規則與下一階
		int memberCount = memberPrices.size();
		List<GroupDiscountRule> rules = groupDiscountRuleService.findActiveByEventId(eventId);
		GroupDiscountRule appliedRule = groupDiscountRuleService.resolveBestRule(eventId, memberCount).orElse(null);

		// 4.逐人計算折抵; discountValue 為減免百分比，每人各自四捨五入到整數 (元)
		// 總折抵 = 每人折抵加總 (與優惠碼逐場型態相同做法)，確保訂單細項加總與訂單金額一致、不需處理尾差
		for (GroupMemberPriceBO memberPrice : memberPrices) {
			BigDecimal discount = appliedRule == null ? BigDecimal.ZERO
					: memberPrice.getOriginalPrice()
							.multiply(BigDecimal.valueOf(appliedRule.getDiscountValue()))
							.divide(HUNDRED, 0, RoundingMode.HALF_UP);
			memberPrice.setDiscount(discount);
			memberPrice.setPayableAmount(memberPrice.getOriginalPrice().subtract(discount).max(BigDecimal.ZERO));
		}

		BigDecimal totalDiscount = memberPrices.stream()
				.map(GroupMemberPriceBO::getDiscount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal finalPrice = memberPrices.stream()
				.map(GroupMemberPriceBO::getPayableAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		GroupOrderVO vo = new GroupOrderVO();
		vo.setRegistrationGroupId(registrationGroup.getRegistrationGroupId());
		vo.setMemberCount(memberCount);
		vo.setEligible(appliedRule != null);
		vo.setMinCount(rules.isEmpty() ? null : rules.get(0).getMinCount());
		vo.setAppliedRule(appliedRule);
		vo.setNextRule(this.findNextRule(rules, memberCount, appliedRule).orElse(null));
		vo.setOriginalTotal(originalTotal);
		vo.setTotalDiscount(totalDiscount);
		vo.setFinalPrice(finalPrice);
		vo.setMemberPrices(memberPrices);
		if (appliedRule != null) {
			vo.setAppliedDiscounts(List.of(AppliedDiscountBO.ofGroup(appliedRule.getGroupDiscountRuleId(),
					appliedRule.getName(), registrationGroup.getInviteCode(), totalDiscount)));
		}
		return vo;
	}

	/**
	 * 組裝團體 VO
	 *
	 * @param registrationGroup
	 * @param viewerMemberId    目前登入者; 後台查詢傳 null
	 * @param fullAccess        是否可看完整資料 (後台)
	 * @return
	 */
	private RegistrationGroupVO toVO(RegistrationGroup registrationGroup, Long viewerMemberId, boolean fullAccess) {

		Event event = eventService.get(registrationGroup.getEventId());
		List<RegistrationGroupMember> groupMembers = registrationGroupMemberService
				.findByGroupId(registrationGroup.getRegistrationGroupId());
		Member leader = memberService.getMember(registrationGroup.getLeaderMemberId());

		boolean isLeader = registrationGroup.getLeaderMemberId().equals(viewerMemberId);
		boolean canSeeDetail = fullAccess || isLeader;
		int memberCount = groupMembers.size();

		List<GroupDiscountRule> rules = groupDiscountRuleService.findActiveByEventId(registrationGroup.getEventId());
		GroupDiscountRule currentRule = groupDiscountRuleService
				.resolveBestRule(registrationGroup.getEventId(), memberCount)
				.orElse(null);

		RegistrationGroupVO vo = new RegistrationGroupVO();
		vo.setRegistrationGroupId(registrationGroup.getRegistrationGroupId());
		vo.setEventId(registrationGroup.getEventId());
		vo.setEventTitle(event == null ? null : event.getTitle());
		vo.setName(registrationGroup.getName());
		vo.setStatus(registrationGroup.getStatus());
		vo.setIsLeader(isLeader);
		vo.setLeaderName(this.displayName(leader));
		vo.setMemberCount(memberCount);
		vo.setCurrentRule(currentRule);
		vo.setNextRule(this.findNextRule(rules, memberCount, currentRule).orElse(null));
		vo.setRules(rules);
		vo.setCreateDate(registrationGroup.getCreateDate());

		if (canSeeDetail) {
			vo.setInviteCode(registrationGroup.getInviteCode());
			vo.setOrdersId(registrationGroup.getOrdersId());
			vo.setMembers(this.toMemberVOList(groupMembers));
		}

		return vo;
	}

	private List<RegistrationGroupMemberVO> toMemberVOList(List<RegistrationGroupMember> groupMembers) {

		Map<Long, Member> memberById = this.loadMembers(groupMembers);
		Map<Long, MemberType> memberTypeById = new HashMap<>();

		return groupMembers.stream().map(groupMember -> {
			Member member = memberById.get(groupMember.getMemberId());
			MemberType memberType = member == null ? null
					: memberTypeById.computeIfAbsent(member.getMemberTypeId(), memberTypeService::get);

			RegistrationGroupMemberVO vo = new RegistrationGroupMemberVO();
			vo.setMemberId(groupMember.getMemberId());
			vo.setName(member == null ? null : this.displayName(member));
			vo.setEmail(member == null ? null : member.getEmail());
			vo.setMemberTypeLabel(memberType == null ? null : memberType.getLabelZh());
			vo.setRole(groupMember.getRole());
			vo.setJoinDate(groupMember.getCreateDate());
			return vo;
		}).toList();
	}

	/**
	 * 找出下一個可升級的規則：門檻比目前人數高、且折扣比目前更多的規則中，門檻最低的那條
	 *
	 * @param rules       依 minCount 由小到大排序
	 * @param memberCount
	 * @param currentRule 可為 null
	 * @return
	 */
	private Optional<GroupDiscountRule> findNextRule(List<GroupDiscountRule> rules, int memberCount,
			GroupDiscountRule currentRule) {
		int currentValue = currentRule == null ? 0 : currentRule.getDiscountValue();
		return rules.stream()
				.filter(rule -> rule.getMinCount() > memberCount && rule.getDiscountValue() > currentValue)
				.findFirst();
	}

	/**
	 * 校驗活動可否團體報名，並回傳活動
	 *
	 * @param eventId
	 * @return
	 */
	private Event validateEventForGroup(Long eventId) {

		// 1.團體報名的整體截止時間 (Setting.lastGroupRegistrationTime)
		if (!Boolean.TRUE.equals(settingService.isGroupRegistrationOpen())) {
			throw new RegistrationClosedException(messageHelper.get(I18nMessageKey.Registration.Group.CLOSED));
		}

		// 2.活動需啟用且開放團體報名
		Event event = eventService.get(eventId);
		if (event == null || !CommonStatusEnum.YES.equals(event.getIsActive())
				|| !CommonStatusEnum.YES.equals(event.getAllowGroupRegistration())) {
			throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.NOT_ALLOWED));
		}

		// 3.活動報名已截止
		if (event.getRegistrationCloseAt() != null && LocalDateTime.now().isAfter(event.getRegistrationCloseAt())) {
			throw new RegistrationClosedException(messageHelper.get(I18nMessageKey.Registration.CLOSED));
		}

		// 4.沒有任何啟用中的規則就沒有團體報名的意義
		if (groupDiscountRuleService.findActiveByEventId(eventId).isEmpty()) {
			throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.NO_RULE));
		}

		return event;
	}

	/**
	 * 校驗會員可加入此活動的團體：尚未報名此活動、也不在此活動的其他團體
	 *
	 * @param memberId
	 * @param event
	 */
	private void validateMemberCanJoin(Long memberId, Event event) {

		if (attendeeEventService.getByMemberAndEvent(memberId, event.getEventId()) != null) {
			throw new EventException(
					messageHelper.get(I18nMessageKey.Registration.EVENT_ALREADY_REGISTERED, (Object) event.getTitle()));
		}

		if (registrationGroupMemberService.isInGroupForEvent(memberId, event.getEventId())) {
			throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.IN_OTHER_GROUP));
		}
	}

	private RegistrationGroup requireGroup(Long registrationGroupId) {
		RegistrationGroup registrationGroup = registrationGroupService.get(registrationGroupId);
		if (registrationGroup == null) {
			throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.NOT_FOUND));
		}
		return registrationGroup;
	}

	private RegistrationGroup requireGroupByInviteCode(String inviteCode) {
		RegistrationGroup registrationGroup = registrationGroupService.getByInviteCode(inviteCode);
		if (registrationGroup == null) {
			throw new RegistrationGroupException(
					messageHelper.get(I18nMessageKey.Registration.Group.INVITE_CODE_INVALID));
		}
		return registrationGroup;
	}

	private RegistrationGroupMember requireMembership(RegistrationGroup registrationGroup, Long memberId) {
		RegistrationGroupMember groupMember = registrationGroupMemberService
				.getByGroupAndMember(registrationGroup.getRegistrationGroupId(), memberId);
		if (groupMember == null) {
			throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.NOT_MEMBER));
		}
		return groupMember;
	}

	private void requireLeader(RegistrationGroup registrationGroup, Long memberId) {
		if (!registrationGroup.getLeaderMemberId().equals(memberId)) {
			throw new RegistrationGroupException(messageHelper.get(I18nMessageKey.Registration.Group.LEADER_ONLY));
		}
	}

	private void requireRecruiting(RegistrationGroup registrationGroup) {
		if (!RegistrationGroupStatusEnum.RECRUITING.equals(registrationGroup.getStatus())) {
			throw new RegistrationGroupException(
					messageHelper.get(I18nMessageKey.Registration.Group.NOT_RECRUITING));
		}
	}

	private Map<Long, Member> loadMembers(List<RegistrationGroupMember> groupMembers) {
		if (groupMembers.isEmpty()) {
			return Collections.emptyMap();
		}
		List<Long> memberIds = groupMembers.stream().map(RegistrationGroupMember::getMemberId).toList();
		return memberService.listByIds(memberIds)
				.stream()
				.collect(Collectors.toMap(Member::getMemberId, Function.identity()));
	}

	/**
	 * 顯示用姓名：有中文姓名時優先，否則為 名 + 姓
	 *
	 * @param member
	 * @return
	 */
	private String displayName(Member member) {
		if (member == null) {
			return "";
		}
		if (StringUtils.isNotBlank(member.getChineseName())) {
			return member.getChineseName().trim();
		}
		return (StringUtils.defaultString(member.getFirstName()) + " "
				+ StringUtils.defaultString(member.getLastName())).trim();
	}

	/**
	 * 產生團體名稱：「負責人名稱 - 活動名稱 - 團體報名」<br>
	 * 例: 王小明 - 2026 年會主場次 - 團體報名<br>
	 * 負責人姓名顯示全名 (不遮罩)，讓成員輸入邀請碼後能確認是誰的團體
	 *
	 * @param leader
	 * @param event
	 * @return
	 */
	private String generateGroupName(Member leader, Event event) {
		String groupName = this.displayName(leader) + " - " + event.getTitle() + " - 團體報名";
		return StringUtils.left(groupName, 255);
	}

	private String groupLockKey(Long registrationGroupId) {
		return GROUP_LOCK_PREFIX + registrationGroupId;
	}

	private String memberLockKey(Long memberId, Long eventId) {
		return MEMBER_LOCK_PREFIX + memberId + ":" + eventId;
	}

	/**
	 * 依序取得分散鎖後，在交易內執行; 交易提交後才釋放鎖
	 *
	 * @param lockKeys 依固定順序 (團體 → 會員) 傳入，避免死鎖
	 * @param action
	 * @return
	 */
	private <T> T withLocks(List<String> lockKeys, Supplier<T> action) {
		List<RLock> acquired = new ArrayList<>();
		try {
			for (String lockKey : lockKeys) {
				RLock lock = redissonClient.getLock(lockKey);
				lock.lock(LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
				acquired.add(lock);
			}
			return transactionTemplate.execute(status -> action.get());
		} finally {
			for (int i = acquired.size() - 1; i >= 0; i--) {
				RLock lock = acquired.get(i);
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		}
	}

	private void runWithLocks(List<String> lockKeys, Runnable action) {
		this.withLocks(lockKeys, () -> {
			action.run();
			return null;
		});
	}

}
