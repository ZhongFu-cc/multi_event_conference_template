package tw.com.conference.service;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddGroupDiscountRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutGroupDiscountRuleDTO;
import tw.com.conference.pojo.entity.GroupDiscountRule;

public interface GroupDiscountRuleService extends IService<GroupDiscountRule> {

	GroupDiscountRule get(Long groupDiscountRuleId);

	/**
	 * 查詢某場活動的所有規則 (含停用)，依最低人數由小到大
	 *
	 * @param eventId
	 * @return
	 */
	List<GroupDiscountRule> findByEventId(Long eventId);

	/**
	 * 查詢某場活動啟用中的規則，依最低人數由小到大
	 *
	 * @param eventId
	 * @return
	 */
	List<GroupDiscountRule> findActiveByEventId(Long eventId);

	/**
	 * 依人數找出可套用的規則<br>
	 * 人數符合 (minCount <= memberCount) 的規則中，取折扣最多的那條
	 *
	 * @param eventId
	 * @param memberCount 團體人數 (含負責人)
	 * @return 未達任何門檻則為空
	 */
	Optional<GroupDiscountRule> resolveBestRule(Long eventId, int memberCount);

	/**
	 * 新增規則<br>
	 * 同一場活動的最低人數不可重複
	 *
	 * @param addGroupDiscountRuleDTO
	 * @return
	 */
	GroupDiscountRule create(AddGroupDiscountRuleDTO addGroupDiscountRuleDTO);

	/**
	 * 修改規則 (活動不可更換)
	 *
	 * @param putGroupDiscountRuleDTO
	 */
	void update(PutGroupDiscountRuleDTO putGroupDiscountRuleDTO);

	void remove(Long groupDiscountRuleId);

}
