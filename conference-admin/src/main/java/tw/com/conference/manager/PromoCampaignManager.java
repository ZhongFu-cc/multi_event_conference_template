package tw.com.conference.manager;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.google.common.collect.Sets;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoCodeTypeEnum;
import tw.com.conference.enums.PromoDiscountTypeEnum;
import tw.com.conference.exception.PromoCodeException;
import tw.com.conference.pojo.DTO.AssignEvent2PromoCampaignDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddPromoCampaignDTO;
import tw.com.conference.pojo.VO.PromoCampaignVO;
import tw.com.conference.pojo.VO.PromoCodeVO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.PromoCampaign;
import tw.com.conference.pojo.entity.PromoCode;
import tw.com.conference.pojo.entity.PromoCodeRedemption;
import tw.com.conference.pojo.excelPojo.PromoCodeExcel;
import tw.com.conference.service.EventService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.PromoCampaignService;
import tw.com.conference.service.PromoCodeEventService;
import tw.com.conference.service.PromoCodeRedemptionService;
import tw.com.conference.service.PromoCodeService;

/**
 * 優惠碼活動的後台管理<br>
 * 負責編排「建立 campaign + 產生碼 + 分配適用活動」等跨 service 的流程
 */
@Component
@RequiredArgsConstructor
public class PromoCampaignManager {

	private final PromoCampaignService promoCampaignService;
	private final PromoCodeService promoCodeService;
	private final PromoCodeEventService promoCodeEventService;
	private final PromoCodeRedemptionService promoCodeRedemptionService;
	private final EventService eventService;
	private final MemberService memberService;

	/**
	 * 建立優惠碼活動，並一併產生碼與分配適用活動
	 *
	 * @param addPromoCampaignDTO
	 * @return
	 */
	@Transactional
	public PromoCampaignVO createCampaign(AddPromoCampaignDTO addPromoCampaignDTO) {

		PromoCodeTypeEnum codeType = addPromoCampaignDTO.getCodeType();
		PromoDiscountTypeEnum discountType = addPromoCampaignDTO.getDiscountType();

		// 1.訂單層態忽略適用範圍，不允許指定活動，避免設了卻不生效造成誤解
		if (!discountType.isPerEvent() && addPromoCampaignDTO.getEventIds() != null
				&& !addPromoCampaignDTO.getEventIds().isEmpty()) {
			throw new PromoCodeException(discountType.getLabel() + " 是針對整張訂單折抵，不可指定適用活動");
		}

		// 2.通用碼只會有一組碼，不接受 quantity
		if (PromoCodeTypeEnum.GENERAL.equals(codeType) && addPromoCampaignDTO.getQuantity() != null
				&& addPromoCampaignDTO.getQuantity() > 1) {
			throw new PromoCodeException("通用碼只會產生 1 組碼，不需指定數量");
		}

		// 3.一次性碼必須指定數量，且不可自訂碼字串 (碼由系統產生)
		if (PromoCodeTypeEnum.UNIQUE.equals(codeType)) {
			if (addPromoCampaignDTO.getQuantity() == null) {
				throw new PromoCodeException("一次性碼必須指定要產生幾組");
			}
			if (addPromoCampaignDTO.getCode() != null && !addPromoCampaignDTO.getCode().isBlank()) {
				throw new PromoCodeException("一次性碼的碼字串由系統產生，不可自訂");
			}
		}

		// 4.建立 campaign 本身 (折扣型態與折扣值的交叉校驗在 service 內)
		PromoCampaign promoCampaign = promoCampaignService.create(addPromoCampaignDTO);
		Long promoCampaignId = promoCampaign.getPromoCampaignId();

		// 5.產生碼
		int maxUses = codeType.getDefaultMaxUses();
		if (PromoCodeTypeEnum.GENERAL.equals(codeType)) {
			if (addPromoCampaignDTO.getCode() != null && !addPromoCampaignDTO.getCode().isBlank()) {
				// 後台自訂碼字串
				promoCodeService.createWithCode(promoCampaignId, addPromoCampaignDTO.getCode(), maxUses);
			} else {
				promoCodeService.batchGenerate(promoCampaignId, addPromoCampaignDTO.getCodePrefix(), 1, maxUses);
			}
		} else {
			promoCodeService.batchGenerate(promoCampaignId, addPromoCampaignDTO.getCodePrefix(),
					addPromoCampaignDTO.getQuantity(), maxUses);
		}

		// 6.分配適用活動 (逐場型態才會有值; 空的代表全場適用)
		if (discountType.isPerEvent() && addPromoCampaignDTO.getEventIds() != null
				&& !addPromoCampaignDTO.getEventIds().isEmpty()) {
			promoCodeEventService.addEventsToCampaign(promoCampaignId,
					new HashSet<>(addPromoCampaignDTO.getEventIds()));
		}

		return this.getCampaignVO(promoCampaignId);
	}

	/**
	 * 為優惠碼活動 分配/移除 適用的活動事件
	 *
	 * @param dto
	 */
	@Transactional
	public void assignEvent2Campaign(AssignEvent2PromoCampaignDTO dto) {

		PromoCampaign promoCampaign = promoCampaignService.get(dto.getPromoCampaignId());
		if (promoCampaign == null) {
			throw new PromoCodeException("優惠碼活動不存在");
		}

		// 訂單層態忽略適用範圍，允許設定只會造成誤解，直接拒絕
		if (!promoCampaign.getDiscountType().isPerEvent()) {
			throw new PromoCodeException(
					promoCampaign.getDiscountType().getLabel() + " 是針對整張訂單折抵，不可指定適用活動");
		}

		// 1.先查詢這個 優惠碼活動 目前適用的 活動事件
		Set<Long> currentEventIdSet = promoCodeEventService.findEventIdsByCampaignId(dto.getPromoCampaignId());

		// 2.拿到該移除的集合 和 該新增的集合
		Set<Long> targetEventIdSet = new HashSet<>(dto.getEventIds());
		Set<Long> eventsToRemove = Sets.difference(currentEventIdSet, targetEventIdSet);
		Set<Long> eventsToAdd = Sets.difference(targetEventIdSet, currentEventIdSet);

		// 3.要移除的eventIds不為空，執行刪除操作
		if (!eventsToRemove.isEmpty()) {
			promoCodeEventService.removeEventsFromCampaign(dto.getPromoCampaignId(), eventsToRemove);
		}

		// 4.要新增的eventIds不為空，開始進行新增操作
		if (!eventsToAdd.isEmpty()) {
			promoCodeEventService.addEventsToCampaign(dto.getPromoCampaignId(), eventsToAdd);
		}
	}

	/**
	 * 移除優惠碼活動 (連帶碼與適用活動)
	 *
	 * @param promoCampaignId
	 */
	@Transactional
	public void removeCampaign(Long promoCampaignId) {
		// 使用紀錄刻意保留 —— 那是稽核資料，不隨 campaign 刪除而消失
		promoCodeEventService.removeByCampaignId(promoCampaignId);
		promoCodeService.removeByCampaignId(promoCampaignId);
		promoCampaignService.remove(promoCampaignId);
	}

	/**
	 * 取得單一優惠碼活動 (含使用統計與適用活動)
	 *
	 * @param promoCampaignId
	 * @return
	 */
	public PromoCampaignVO getCampaignVO(Long promoCampaignId) {

		PromoCampaign promoCampaign = promoCampaignService.get(promoCampaignId);
		if (promoCampaign == null) {
			throw new PromoCodeException("優惠碼活動不存在");
		}

		return this.toCampaignVO(promoCampaign, promoCodeService.findByCampaignId(promoCampaignId));
	}

	/**
	 * 取得所有優惠碼活動
	 *
	 * @return
	 */
	public List<PromoCampaignVO> getCampaignVOList() {
		return promoCampaignService.list()
				.stream()
				.map(promoCampaign -> this.toCampaignVO(promoCampaign,
						promoCodeService.findByCampaignId(promoCampaign.getPromoCampaignId())))
				.toList();
	}

	/**
	 * 取得某優惠碼活動底下所有的碼及其使用情形
	 *
	 * @param promoCampaignId
	 * @return
	 */
	public List<PromoCodeVO> getCodeVOList(Long promoCampaignId) {

		List<PromoCode> promoCodeList = promoCodeService.findByCampaignId(promoCampaignId);
		if (promoCodeList.isEmpty()) {
			return Collections.emptyList();
		}

		// 以 promoCodeId 為 key 的使用紀錄; 一次性碼最多一筆，通用碼取最後一筆代表
		Map<Long, PromoCodeRedemption> redemptionByCodeId = promoCodeRedemptionService
				.findByCampaignId(promoCampaignId)
				.stream()
				.collect(Collectors.toMap(PromoCodeRedemption::getPromoCodeId, redemption -> redemption,
						(a, b) -> b));

		// 一次撈出使用者資料
		Map<Long, Member> memberMap = memberService.getMemberMapByIds(redemptionByCodeId.values()
				.stream()
				.map(PromoCodeRedemption::getMemberId)
				.distinct()
				.toList());

		return promoCodeList.stream().map(promoCode -> {
			PromoCodeVO vo = new PromoCodeVO();
			vo.setPromoCodeId(promoCode.getPromoCodeId());
			vo.setCode(promoCode.getCode());
			vo.setMaxUses(promoCode.getMaxUses());
			vo.setUsedCount(promoCode.getUsedCount());
			vo.setIsUsed(CommonStatusEnum
					.fromBoolean(promoCode.getUsedCount() != null && promoCode.getUsedCount() > 0));

			PromoCodeRedemption redemption = redemptionByCodeId.get(promoCode.getPromoCodeId());
			if (redemption != null) {
				vo.setMemberId(redemption.getMemberId());
				vo.setOrdersId(redemption.getOrdersId());
				vo.setDiscountAmount(redemption.getDiscountAmount());
				vo.setRedemptionStatus(redemption.getStatus());
				vo.setUsedDate(redemption.getCreateDate());

				Member member = memberMap.get(redemption.getMemberId());
				if (member != null) {
					vo.setMemberName(memberService.getOnlyMemberName(member));
					vo.setMemberEmail(member.getEmail());
				}
			}

			return vo;
		}).toList();
	}

	/**
	 * 下載某優惠碼活動的碼清單 Excel (供後台發送給 VIP 分送)
	 *
	 * @param response
	 * @param promoCampaignId
	 * @throws IOException
	 */
	public void downloadCodesExcel(HttpServletResponse response, Long promoCampaignId) throws IOException {

		PromoCampaign promoCampaign = promoCampaignService.get(promoCampaignId);
		if (promoCampaign == null) {
			throw new PromoCodeException("優惠碼活動不存在");
		}

		// 1.設置Excel 檔案資訊
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		// 这里URLEncoder.encode可以防止中文乱码 ， 和easyexcel没有关系
		String fileName = URLEncoder.encode("優惠碼清單-" + promoCampaign.getName(), "UTF-8")
				.replaceAll("\\+", "%20");
		response.setHeader("Content-disposition", "attachment;filename*=" + fileName + ".xlsx");

		// 2.沿用 VO 的組裝邏輯，避免兩邊的使用狀態判斷不一致
		String discountDescription = this.describeDiscount(promoCampaign);

		List<PromoCodeExcel> excelData = this.getCodeVOList(promoCampaignId).stream().map(vo -> {
			PromoCodeExcel excel = new PromoCodeExcel();
			excel.setCode(vo.getCode());
			excel.setCampaignName(promoCampaign.getName());
			excel.setDiscountDescription(discountDescription);
			excel.setStatus(CommonStatusEnum.YES.equals(vo.getIsUsed())
					? (vo.getRedemptionStatus() == null ? "已使用" : vo.getRedemptionStatus().getLabel())
					: "未使用");
			excel.setMemberName(vo.getMemberName());
			excel.setMemberEmail(vo.getMemberEmail());
			excel.setDiscountAmount(vo.getDiscountAmount());
			// Excel 的數字欄位遇到雪花ID會失去精度，一律轉字串
			excel.setOrdersId(vo.getOrdersId() == null ? null : vo.getOrdersId().toString());
			excel.setUsedDate(vo.getUsedDate());
			return excel;
		}).toList();

		// 3.輸出成Excel
		EasyExcel.write(response.getOutputStream(), PromoCodeExcel.class).sheet("優惠碼清單").doWrite(excelData);
	}

	/**
	 * 組裝 PromoCampaignVO
	 *
	 * @param promoCampaign
	 * @param promoCodeList
	 * @return
	 */
	private PromoCampaignVO toCampaignVO(PromoCampaign promoCampaign, List<PromoCode> promoCodeList) {

		PromoCampaignVO vo = new PromoCampaignVO();
		vo.setPromoCampaignId(promoCampaign.getPromoCampaignId());
		vo.setName(promoCampaign.getName());
		vo.setCodeType(promoCampaign.getCodeType());
		vo.setDiscountType(promoCampaign.getDiscountType());
		vo.setDiscountTypeLabel(promoCampaign.getDiscountType().getLabel());
		vo.setDiscountValue(promoCampaign.getDiscountValue());
		vo.setDateFrom(promoCampaign.getDateFrom());
		vo.setDateTo(promoCampaign.getDateTo());
		vo.setIsActive(promoCampaign.getIsActive());

		vo.setTotalCodeCount(promoCodeList.size());
		int usedCount = promoCodeList.stream()
				.mapToInt(promoCode -> promoCode.getUsedCount() == null ? 0 : promoCode.getUsedCount())
				.sum();
		vo.setUsedCount(usedCount);

		// 通用碼不限次數，沒有「剩餘組數」的概念
		boolean unlimited = promoCodeList.stream()
				.anyMatch(promoCode -> promoCode.getMaxUses() != null && promoCode.getMaxUses() == 0);
		if (unlimited) {
			vo.setRemainingCodeCount(null);
		} else {
			long remaining = promoCodeList.stream()
					.filter(promoCode -> promoCode.getUsedCount() == null || promoCode.getUsedCount() == 0)
					.count();
			vo.setRemainingCodeCount((int) remaining);
		}

		// 適用活動; 訂單層態不使用，回空列表
		if (promoCampaign.getDiscountType().isPerEvent()) {
			List<Long> eventIds = promoCodeEventService
					.findEventIdsByCampaignId(promoCampaign.getPromoCampaignId())
					.stream()
					.toList();
			vo.setEventIds(eventIds);
			vo.setEventTitles(eventIds.stream().map(eventId -> {
				Event event = eventService.get(eventId);
				return event == null ? null : event.getTitle();
			}).toList());
		} else {
			vo.setEventIds(Collections.emptyList());
			vo.setEventTitles(Collections.emptyList());
		}

		return vo;
	}

	/**
	 * 把折扣型態與折扣值組成人看得懂的描述 (Excel 用)
	 *
	 * @param promoCampaign
	 * @return
	 */
	private String describeDiscount(PromoCampaign promoCampaign) {

		PromoDiscountTypeEnum discountType = promoCampaign.getDiscountType();
		BigDecimal discountValue = promoCampaign.getDiscountValue();

		return switch (discountType) {
			case FREE -> "適用活動免費";
			case AMOUNT -> "適用活動每場折抵 " + discountValue.stripTrailingZeros().toPlainString() + " 元";
			case PERCENT -> "適用活動每場折抵 " + discountValue.stripTrailingZeros().toPlainString() + "%";
			case ALL_AMOUNT -> "整張訂單折抵 " + discountValue.stripTrailingZeros().toPlainString() + " 元";
			case ALL_PERCENT -> "整張訂單折抵 " + discountValue.stripTrailingZeros().toPlainString() + "%";
		};
	}

}
