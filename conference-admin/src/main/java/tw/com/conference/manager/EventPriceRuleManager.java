package tw.com.conference.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.exception.EventException;
import tw.com.conference.pojo.VO.PriceDashboardVO;
import tw.com.conference.pojo.VO.PriceDashboardVO.PriceRowVO;
import tw.com.conference.pojo.VO.PriceDashboardVO.PriceTableVO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.service.DiscountPackageItemService;
import tw.com.conference.service.EventService;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.service.PricingRuleService;

@Component
@RequiredArgsConstructor
public class EventPriceRuleManager {

	private final EventService eventService;
	private final MemberTypeService memberTypeService;
	private final PricingRuleService pricingRuleService;
	private final DiscountPackageItemService discountPackageItemService;

	/**
	 * 構建價格表
	 * 
	 * @param allRules    所有價格規則
	 * @param nationality 當前國籍
	 * @param headers     表頭欄位
	 * @param memberTypes 所有身分類別
	 * @return
	 */
	private PriceTableVO buildTable(List<PricingRule> allRules, NationalityEnum nationality, List<String> headers,
			List<MemberType> memberTypes) {

		// 創建要返回的VO對象
		PriceTableVO table = new PriceTableVO();

		// 設定表格上方的t-header
		table.setDateRangeHeaders(headers);

		List<PriceRowVO> rows = new ArrayList<>();

		// 過濾出當前國籍的規則
		List<PricingRule> filteredRules = allRules.stream()
				.filter(r -> r.getNationality().equals(nationality))
				.collect(Collectors.toList());

		for (MemberType type : memberTypes) {
			List<BigDecimal> prices = new ArrayList<>();

			for (String header : headers) {
				// 嘗試尋找：特定身分 + 特定區間
				PricingRule match = filteredRules.stream()
						.filter(r -> (type.getMemberTypeId().equals(r.getMemberTypeId()))
								&& header.equals(r.getDateFrom() + "~" + r.getDateTo()))
						.findFirst()
						// 如果找不到，觸發 Fallback：尋找 MemberTypeId 為 NULL 的通用規則
						.orElseGet(() -> filteredRules.stream()
								.filter(r -> r.getMemberTypeId() == null
										&& header.equals(r.getDateFrom() + "~" + r.getDateTo()))
								.findFirst()
								.orElse(null));

				// 價格放入 , null 為沒匹配到任何規則
				prices.add(match != null ? match.getAmount() : null);
			}
			rows.add(new PriceRowVO(type.getLabelZh(), prices));
		}
		table.setRows(rows);
		return table;
	}

	/**
	 * 查詢該活動所有規則<br>
	 * 並獲得表格式的資料顯示<br>
	 * 有本國、外國兩個表格的資料
	 * 
	 * @param eventId
	 * @return
	 */
	public PriceDashboardVO getPriceDashboard(Long eventId) {

		// 1. 查詢該活動所有的價格規則
		List<PricingRule> allRules = pricingRuleService.findPricingRulesByEventId(eventId);

		// 2. 拿到所有不重複的日期區間（作為橫軸表頭）
		List<String> distinctDateRanges = allRules.stream()
				.map(r -> r.getDateFrom() + "~" + r.getDateTo())
				.distinct()
				.collect(Collectors.toList());

		// 3. 拿到所有需要顯示的會員身分 (這裡可以從 member_type 表查，或從 rule 裡提取)
		List<MemberType> memberTypes = memberTypeService.list();

		// 4. 分別處理本國與外籍
		PriceDashboardVO vo = new PriceDashboardVO();
		vo.setDomesticTable(buildTable(allRules, NationalityEnum.DOMESTIC, distinctDateRanges, memberTypes));
		vo.setInternationalTable(buildTable(allRules, NationalityEnum.INTERNATIONAL, distinctDateRanges, memberTypes));

		return vo;

	};

	/**
	 * 刪除活動事件，包含相關的組合優惠、價格策略等...
	 * 
	 * @param eventId
	 */
	public void removeEvent(Long eventId) {

		Event event = eventService.get(eventId);

		// 如果Event 是 main活動,不可刪除
		if (event.getIsMain().getBooleanValue()) {
			throw new EventException("Main活動 不可刪除");
		}

		// 刪除事件活動 相關的 組合優惠
		discountPackageItemService.removeByEventId(eventId);

		// 刪除事件活動 相關的 價格策略
		pricingRuleService.removeByEventId(eventId);

		// 刪除事件活動 本身
		eventService.remove(eventId);
	}

}
