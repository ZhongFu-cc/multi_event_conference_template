package tw.com.conference.service;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.enums.RegistrationGroupStatusEnum;
import tw.com.conference.pojo.entity.RegistrationGroup;

public interface RegistrationGroupService extends IService<RegistrationGroup> {

	RegistrationGroup get(Long registrationGroupId);

	/**
	 * 以邀請碼查詢 (不分大小寫)
	 *
	 * @param inviteCode
	 * @return 查不到則為 null
	 */
	RegistrationGroup getByInviteCode(String inviteCode);

	/**
	 * 以訂單ID查詢團體
	 *
	 * @param ordersId
	 * @return 不是團體訂單則為 null
	 */
	RegistrationGroup getByOrdersId(Long ordersId);

	List<RegistrationGroup> findByIds(Collection<Long> registrationGroupIds);

	/**
	 * 查詢某場活動的所有團體 (新到舊)
	 *
	 * @param eventId
	 * @return
	 */
	List<RegistrationGroup> findByEventId(Long eventId);

	/**
	 * 建立團體 (不含成員，由 RegistrationGroupManager 編排)<br>
	 * 狀態為招募中，邀請碼自動產生
	 *
	 * @param eventId
	 * @param leaderMemberId
	 * @param name
	 * @return
	 */
	RegistrationGroup create(Long eventId, Long leaderMemberId, String name);

	/**
	 * 重新產生邀請碼，舊碼立即失效
	 *
	 * @param registrationGroup
	 * @return 新的邀請碼
	 */
	String regenerateInviteCode(RegistrationGroup registrationGroup);

	/**
	 * 轉換團體狀態並寫入訂單ID (條件式 UPDATE)
	 *
	 * @param registrationGroupId
	 * @param from
	 * @param to
	 * @param ordersId            可為 null (退回招募中時清空)
	 * @return 是否轉換成功; false 代表狀態已被其他請求改變
	 */
	boolean transitStatus(Long registrationGroupId, RegistrationGroupStatusEnum from, RegistrationGroupStatusEnum to,
			Long ordersId);

	/**
	 * 團體訂單被刪除時的處理：<br>
	 * 刪除成員在該活動的報名紀錄，團體退回招募中<br>
	 * 不是團體訂單則不做任何事
	 *
	 * @param ordersId
	 */
	void releaseByOrdersId(Long ordersId);

	void remove(Long registrationGroupId);

}
