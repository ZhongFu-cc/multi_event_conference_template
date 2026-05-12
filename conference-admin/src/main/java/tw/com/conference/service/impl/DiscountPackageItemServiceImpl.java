package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.mapper.DiscountPackageItemMapper;
import tw.com.conference.mapper.DiscountPackageMapper;
import tw.com.conference.mapper.EventMapper;
import tw.com.conference.pojo.entity.DiscountPackage;
import tw.com.conference.pojo.entity.DiscountPackageItem;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.service.DiscountPackageItemService;

/**
 * <p>
 * 活動組合優惠 - 適用活動場次 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Service
@RequiredArgsConstructor
public class DiscountPackageItemServiceImpl extends ServiceImpl<DiscountPackageItemMapper, DiscountPackageItem>
		implements DiscountPackageItemService {

	private final EventMapper eventMapper;
	private final DiscountPackageMapper discountPackageMapper;

	@Override
	public List<DiscountPackageItem> findPackageItemByPackageId(Long packageId) {
		return baseMapper.selectByPackageId(packageId);
	}

	@Override
	public Map<Long, List<Event>> groupEventsByPackageId(Collection<Long> packageIds) {
		// 空值判斷
		if (packageIds != null && packageIds.isEmpty()) {
			return Collections.emptyMap();
		}

		// 1.查詢所有關聯
		List<DiscountPackageItem> packageItems = baseMapper.selectByPackageIds(packageIds);
		// 沒有關聯直接返回空映射
		if (packageItems.isEmpty()) {
			return Collections.emptyMap();
		}

		// 2. 按 packageId 分組，收集 eventId
		Map<Long, List<Long>> packageId2EventIds = packageItems.stream()
				.collect(Collectors.groupingBy(DiscountPackageItem::getDiscountPackageId,
						Collectors.mapping(DiscountPackageItem::getEventId, Collectors.toList())));

		// 3. 收集所有 eventId，獲取map中所有value,兩層List(Collection<List<Long>>)要拆開
		Set<Long> allEventIds = packageId2EventIds.values().stream().flatMap(List::stream).collect(Collectors.toSet());

		// 4. 批量查詢所有 Event，並組成映射關係evnetId:Event
		Map<Long, Event> eventMap = eventMapper.selectBatchIds(allEventIds)
				.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Event::getEventId, Function.identity()));

		// 5. 構建最終結果：package -> List<Event>
		Map<Long, List<Event>> result = new HashMap<>();

		packageId2EventIds.forEach((packageId, eventIds) -> {
			List<Event> tags = eventIds.stream()
					.map(eventMap::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			result.put(packageId, tags);
		});

		return result;
	}

	@Override
	public Map<Long, List<Event>> groupEventsByPackageId(List<DiscountPackage> packages) {

		// 抽取packagIds
		Set<Long> packageIds = packages.stream().map(DiscountPackage::getDiscountPackageId).collect(Collectors.toSet());
		// 獲取以packageId為鍵,eventList為值的方式返回值
		return this.groupEventsByPackageId(packageIds);

	}

	@Override
	public List<DiscountPackageItem> findPackageItemByEventIds(Collection<Long> eventIds) {
		// 空值判斷
		if (eventIds != null && eventIds.isEmpty()) {
			return Collections.emptyList();
		}

		return baseMapper.selectByEventIds(eventIds);
	}

	@Override
	public void addPackageItem(Long packageId, Long eventId) {
		DiscountPackageItem discountPackageItem = new DiscountPackageItem();
		discountPackageItem.setDiscountPackageId(packageId);
		discountPackageItem.setEventId(eventId);
		baseMapper.insert(discountPackageItem);

	}

	@Override
	public void addEventsToPackage(Long packageId, Collection<Long> eventsToAdd) {

		// 1.建立多個新連結
		List<DiscountPackageItem> discountPackageItems = eventsToAdd.stream().map(eventId -> {
			DiscountPackageItem discountPackageItem = new DiscountPackageItem();
			discountPackageItem.setDiscountPackageId(packageId);
			discountPackageItem.setEventId(eventId);
			return discountPackageItem;
		}).collect(Collectors.toList());

		// 2.批量新增
		this.saveBatch(discountPackageItems);

	}

	@Override
	public void removeByPackageId(Long packageId) {
		baseMapper.deleteByPackageId(packageId);
	}

	@Override
	public void removeByEventId(Long eventId) {
		baseMapper.deleteByEventId(eventId);
	}

	@Override
	public void addPackagesToEvent(Long eventId, Collection<Long> packagesToAdd) {
		// 1.建立多個新連結
		List<DiscountPackageItem> discountPackageItems = packagesToAdd.stream().map(packageId -> {
			DiscountPackageItem discountPackageItem = new DiscountPackageItem();
			discountPackageItem.setDiscountPackageId(packageId);
			discountPackageItem.setEventId(eventId);
			return discountPackageItem;
		}).collect(Collectors.toList());

		// 2.批量新增
		this.saveBatch(discountPackageItems);
	}

	@Override
	public void removeEventsFromPackage(Long packageId, Collection<Long> eventsToRemove) {
		// 空值判斷
		if (eventsToRemove != null && eventsToRemove.isEmpty()) {
			return;
		}

		// 刪除packageId 和 EventIds 符合的Item
		baseMapper.deleteByPackageIdAndEventIds(packageId, eventsToRemove);

	}

	@Override
	public void removePackagesFromEvent(Long eventId, Collection<Long> packagesToRemove) {
		// 空值判斷
		if (packagesToRemove != null && packagesToRemove.isEmpty()) {
			return;
		}

		// 刪除eventId 和 packageIds 符合的Item
		baseMapper.deleteByEventIdsAndPackageId(eventId, packagesToRemove);

	}

}
