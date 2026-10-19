package tw.com.conference.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.entity.PromoCodeEvent;

public interface PromoCodeEventService extends IService<PromoCodeEvent> {

	/**
	 * 查詢某優惠碼活動的適用活動IDs
	 *
	 * @param promoCampaignId
	 * @return 空集合代表全場適用
	 */
	Set<Long> findEventIdsByCampaignId(Long promoCampaignId);

	List<PromoCodeEvent> findByCampaignId(Long promoCampaignId);

	/**
	 * 為優惠碼活動新增適用活動
	 *
	 * @param promoCampaignId
	 * @param eventIds
	 */
	void addEventsToCampaign(Long promoCampaignId, Collection<Long> eventIds);

	/**
	 * 從優惠碼活動移除指定的適用活動
	 *
	 * @param promoCampaignId
	 * @param eventIds
	 */
	void removeEventsFromCampaign(Long promoCampaignId, Collection<Long> eventIds);

	/**
	 * 移除某優惠碼活動的全部適用活動
	 *
	 * @param promoCampaignId
	 */
	void removeByCampaignId(Long promoCampaignId);

	/**
	 * 刪除活動事件時，連帶清理所有優惠碼活動對它的適用設定
	 *
	 * @param eventId
	 */
	void removeByEventId(Long eventId);

}
