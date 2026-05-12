package tw.com.conference.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import tw.com.conference.pojo.DTO.AssignEvent2PackageDTO;
import tw.com.conference.pojo.entity.DiscountPackageItem;
import tw.com.conference.service.DiscountPackageItemService;
import tw.com.conference.service.DiscountPackageRuleService;
import tw.com.conference.service.DiscountPackageService;

@Component
@RequiredArgsConstructor
public class DiscountPackageManager {

	private final DiscountPackageService discountPackageService;
	private final DiscountPackageItemService discountPackageItemService;
	private final DiscountPackageRuleService discountPackageRuleService;

	/**
	 * 移除優惠組合
	 * 
	 * @param discountPackageId
	 */
	public void removeDiscountPackage(Long discountPackageId) {

		// 刪除優惠組合價格規則
		discountPackageRuleService.removeByDiscountPackageId(discountPackageId);

		// 刪除優惠組合池內的item 
		discountPackageItemService.removeByPackageId(discountPackageId);

		// 最終刪除優惠組合池
		discountPackageService.remove(discountPackageId);
	}

	/**
	 * 為優惠組合分配適用的活動事件
	 * 
	 * @param assignEvent2PackageDTO
	 */
	public void assignEvent2Package(AssignEvent2PackageDTO assignEvent2PackageDTO) {

		// 1.先查詢這個 優惠組合 目前適用的 活動事件
		List<DiscountPackageItem> currentPackageItems = discountPackageItemService
				.findPackageItemByPackageId(assignEvent2PackageDTO.getDiscountPackageId());

		// 2.獲取當前的 活動事件ID 集合
		Set<Long> currentEventIdSet = currentPackageItems.stream()
				.map(DiscountPackageItem::getEventId)
				.collect(Collectors.toSet());

		// 3.拿到該移除的集合 和 該新增的集合
		Set<Long> eventsToRemove = Sets.difference(currentEventIdSet,
				new HashSet<>(assignEvent2PackageDTO.getEventIds()));
		Set<Long> eventsToAdd = Sets.difference(new HashSet<>(assignEvent2PackageDTO.getEventIds()), currentEventIdSet);

		// 4. 要移除的eventIds不為空，執行刪除操作
		if (!eventsToRemove.isEmpty()) {
			discountPackageItemService.removeEventsFromPackage(assignEvent2PackageDTO.getDiscountPackageId(),
					eventsToRemove);
		}

		// 5. 要新增的eventIds不為空，開始進行新增操作
		if (!eventsToAdd.isEmpty()) {
			discountPackageItemService.addEventsToPackage(assignEvent2PackageDTO.getDiscountPackageId(), eventsToAdd);
		}

	}

}
