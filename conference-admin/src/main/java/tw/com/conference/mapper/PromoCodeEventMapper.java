package tw.com.conference.mapper;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.PromoCodeEvent;

/**
 * <p>
 * 優惠碼活動-適用活動事件 Mapper 接口
 * </p>
 */
public interface PromoCodeEventMapper extends BaseMapper<PromoCodeEvent> {

	/**
	 * 查詢某優惠碼活動的適用活動
	 *
	 * @param promoCampaignId
	 * @return 空清單代表全場適用
	 */
	default List<PromoCodeEvent> selectByCampaignId(Long promoCampaignId) {
		LambdaQueryWrapper<PromoCodeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeEvent::getPromoCampaignId, promoCampaignId);
		return this.selectList(queryWrapper);
	}

	/**
	 * 移除某優惠碼活動的全部適用活動
	 *
	 * @param promoCampaignId
	 */
	default void deleteByCampaignId(Long promoCampaignId) {
		LambdaQueryWrapper<PromoCodeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeEvent::getPromoCampaignId, promoCampaignId);
		this.delete(queryWrapper);
	}

	/**
	 * 移除某優惠碼活動中指定的適用活動
	 *
	 * @param promoCampaignId
	 * @param eventIds
	 */
	default void deleteByCampaignIdAndEventIds(Long promoCampaignId, Collection<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return;
		}
		LambdaQueryWrapper<PromoCodeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeEvent::getPromoCampaignId, promoCampaignId)
				.in(PromoCodeEvent::getEventId, eventIds);
		this.delete(queryWrapper);
	}

	/**
	 * 移除所有優惠碼活動中，與該活動事件相關的適用設定 (刪除 event 時連帶清理)
	 *
	 * @param eventId
	 */
	default void deleteByEventId(Long eventId) {
		LambdaQueryWrapper<PromoCodeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCodeEvent::getEventId, eventId);
		this.delete(queryWrapper);
	}

}
