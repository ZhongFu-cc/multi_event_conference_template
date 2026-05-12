package tw.com.conference.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.entity.DiscountPackage;
import tw.com.conference.pojo.entity.DiscountPackageItem;
import tw.com.conference.pojo.entity.Event;

/**
 * <p>
 * 活動組合優惠 - 適用活動場次 服务类
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
public interface DiscountPackageItemService extends IService<DiscountPackageItem> {

	/**
	 * 查詢當前 活動組合 適用的活動事件關聯
	 * 
	 * @param packageId
	 * @return
	 */
	List<DiscountPackageItem> findPackageItemByPackageId(Long packageId);

	/**
	 * 根據 packageIds 獲取符合的event , 以packageId為鍵,eventList為值的方式返回值
	 * 
	 * @param packageIds
	 * @return
	 */
	Map<Long, List<Event>> groupEventsByPackageId(Collection<Long> packageIds);

	/**
	 * 根據 packageIds 獲取符合的event , 以packageId為鍵,eventList為值的方式返回值
	 * 
	 * @param packages
	 * @return key 為 packageId , value 為eventList
	 */
	Map<Long, List<Event>> groupEventsByPackageId(List<DiscountPackage> packages);

	/**
	 * 根據 eventIds 集合， 查詢與之有關的所有PackageItem關聯
	 * 
	 * @param eventIds
	 * @return
	 */
	List<DiscountPackageItem> findPackageItemByEventIds(Collection<Long> eventIds);

	/**
	 * 透過 packageId 和 eventId 建立關聯
	 * 
	 * @param packageId 優惠組合ID
	 * @param eventId   活動事件ID
	 */
	void addPackageItem(Long packageId, Long eventId);

	/**
	 * 為package建立多個event關聯
	 * 
	 * @param packageId
	 * @param eventIds
	 */
	void addEventsToPackage(Long packageId, Collection<Long> eventsToAdd);

	/**
	 * 根據優惠價格規則 ID 增加多個優惠組合 關聯
	 * 
	 * @param eventId
	 * @param packagesToAdd
	 */
	void addPackagesToEvent(Long eventId, Collection<Long> packagesToAdd);

	/**
	 * 刪除包含此 優惠組合 所有的關聯
	 * 
	 * @param packageId
	 */
	void removeByPackageId(Long packageId);
	
	/**
	 * 刪除包含此 活動事件 所有的關聯
	 * 
	 * @param eventId
	 */
	void removeByEventId(Long eventId);
	
	/**
	 * 根據優惠組合 ID 刪除多個優惠價格規則關聯
	 * 
	 * @param packageId
	 * @param eventsToRemove
	 */
	void removeEventsFromPackage(Long packageId, Collection<Long> eventsToRemove);

	/**
	 * 根據優惠價格規則 ID 刪除多個優惠組合 關聯
	 * 
	 * @param eventId
	 * @param packagesToRemove
	 */
	void removePackagesFromEvent(Long eventId, Collection<Long> packagesToRemove);

}
