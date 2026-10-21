package tw.com.conference.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.GroupDiscountRuleConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.exception.RegistrationGroupException;
import tw.com.conference.mapper.GroupDiscountRuleMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddGroupDiscountRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutGroupDiscountRuleDTO;
import tw.com.conference.pojo.entity.GroupDiscountRule;
import tw.com.conference.service.EventService;
import tw.com.conference.service.GroupDiscountRuleService;

@Service
@RequiredArgsConstructor
public class GroupDiscountRuleServiceImpl extends ServiceImpl<GroupDiscountRuleMapper, GroupDiscountRule>
		implements GroupDiscountRuleService {

	private final GroupDiscountRuleConvert groupDiscountRuleConvert;
	private final EventService eventService;

	@Override
	public GroupDiscountRule get(Long groupDiscountRuleId) {
		return baseMapper.selectById(groupDiscountRuleId);
	}

	@Override
	public List<GroupDiscountRule> findByEventId(Long eventId) {
		return baseMapper.selectByEventId(eventId);
	}

	@Override
	public List<GroupDiscountRule> findActiveByEventId(Long eventId) {
		return baseMapper.selectActiveByEventId(eventId);
	}

	@Override
	public Optional<GroupDiscountRule> resolveBestRule(Long eventId, int memberCount) {
		// 折扣相同時取門檻較高的那條，讓顯示的規則名稱與實際人數較貼近
		return this.findActiveByEventId(eventId)
				.stream()
				.filter(rule -> rule.getMinCount() <= memberCount)
				.max(Comparator.comparing(GroupDiscountRule::getDiscountValue)
						.thenComparing(GroupDiscountRule::getMinCount));
	}

	@Override
	public GroupDiscountRule create(AddGroupDiscountRuleDTO addGroupDiscountRuleDTO) {

		if (eventService.get(addGroupDiscountRuleDTO.getEventId()) == null) {
			throw new RegistrationGroupException("活動不存在");
		}

		this.validateUniqueMinCount(addGroupDiscountRuleDTO.getEventId(), addGroupDiscountRuleDTO.getMinCount(),
				null);

		GroupDiscountRule groupDiscountRule = groupDiscountRuleConvert.addDTOToEntity(addGroupDiscountRuleDTO);

		// 未指定啟用狀態時預設啟用
		if (groupDiscountRule.getIsActive() == null) {
			groupDiscountRule.setIsActive(CommonStatusEnum.YES);
		}

		baseMapper.insert(groupDiscountRule);

		return groupDiscountRule;
	}

	@Override
	public void update(PutGroupDiscountRuleDTO putGroupDiscountRuleDTO) {

		GroupDiscountRule existing = this.get(putGroupDiscountRuleDTO.getGroupDiscountRuleId());
		if (existing == null) {
			throw new RegistrationGroupException("團體優惠規則不存在");
		}

		this.validateUniqueMinCount(existing.getEventId(), putGroupDiscountRuleDTO.getMinCount(),
				existing.getGroupDiscountRuleId());

		// eventId 不在 PutDTO 內，不會被改動
		GroupDiscountRule groupDiscountRule = groupDiscountRuleConvert.putDTOToEntity(putGroupDiscountRuleDTO);
		baseMapper.updateById(groupDiscountRule);
	}

	@Override
	public void remove(Long groupDiscountRuleId) {
		baseMapper.deleteById(groupDiscountRuleId);
	}

	/**
	 * 同一場活動的最低人數不可重複，否則同人數會命中兩條規則
	 *
	 * @param eventId
	 * @param minCount
	 * @param excludeRuleId 修改時排除自己，新增時傳 null
	 */
	private void validateUniqueMinCount(Long eventId, Integer minCount, Long excludeRuleId) {
		boolean duplicated = this.findByEventId(eventId)
				.stream()
				.filter(rule -> !Objects.equals(rule.getGroupDiscountRuleId(), excludeRuleId))
				.anyMatch(rule -> rule.getMinCount().equals(minCount));
		if (duplicated) {
			throw new RegistrationGroupException("此活動已有最低人數為 " + minCount + " 人的規則");
		}
	}

}
