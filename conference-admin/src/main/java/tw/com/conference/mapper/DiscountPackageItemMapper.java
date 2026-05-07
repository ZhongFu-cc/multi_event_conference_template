package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.DiscountPackageItem;

/**
 * <p>
 * 活動組合優惠 - 適用活動場次 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
public interface DiscountPackageItemMapper extends BaseMapper<DiscountPackageItem> {

	
	/**
	 * 根據 packageIds 查詢符合 Item
	 * 
	 * @param packageIds
	 * @return
	 */
	default List<DiscountPackageItem> selectByPackageIds(Collection<Long> packageIds) {
		LambdaQueryWrapper<DiscountPackageItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(DiscountPackageItem::getDiscountPackageId, packageIds);
		return this.selectList(queryWrapper);

	}
	
	/**
	 * 根據 eventIds 查詢符合 Item
	 * 
	 * @param eventIds
	 * @return
	 */
	default List<DiscountPackageItem> selectByEventIds(Collection<Long> eventIds) {
		LambdaQueryWrapper<DiscountPackageItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(DiscountPackageItem::getEventId, eventIds);
		return this.selectList(queryWrapper);

	}

	/**
	 * 刪除packageId 和 EventIds 符合的Item
	 * 
	 * @param packageId
	 * @param eventIds
	 */
	default void deleteByPackageIdAndEventIds(Long packageId, Collection<Long> eventIds) {
		LambdaQueryWrapper<DiscountPackageItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DiscountPackageItem::getDiscountPackageId, packageId)
				.in(DiscountPackageItem::getEventId, eventIds);
		this.delete(queryWrapper);
	}

	/**
	 * 刪除eventId 和 packageIds 符合的Item
	 * 
	 * @param eventId
	 * @param packageIds
	 */
	default void deleteByEventIdsAndPackageId(Long eventId, Collection<Long> packageIds) {
		LambdaQueryWrapper<DiscountPackageItem> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DiscountPackageItem::getEventId, eventId)
				.in(DiscountPackageItem::getDiscountPackageId, packageIds);
		this.delete(queryWrapper);
	}

}
