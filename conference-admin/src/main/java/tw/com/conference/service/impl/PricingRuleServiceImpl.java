package tw.com.conference.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.PricingRuleConvert;
import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.exception.PricingRuleException;
import tw.com.conference.mapper.PricingRuleMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddPricingRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPricingRuleDTO;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.service.PricingRuleService;

/**
 * <p>
 * 價格規則表 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Service
@RequiredArgsConstructor
public class PricingRuleServiceImpl extends ServiceImpl<PricingRuleMapper, PricingRule> implements PricingRuleService {

	private final PricingRuleConvert pricingRuleConvert;

	@Override
	public boolean existAny() {
		return baseMapper.existAnyPricingRule();
	}

	@Override
	public PricingRule get(Long pricingRuleId) {
		PricingRule pricingRule = baseMapper.selectById(pricingRuleId);
		return pricingRule;
	}

	@Override
	public List<PricingRule> findPricingRulesByEventId(Long eventId) {
		// 查詢該活動所有的價格規則
		return baseMapper.selectAllRulesByEvnetId(eventId);

	}

	@Override
	public PricingRule resolvePricingRule(Long eventId, Long memberTypeId, NationalityEnum nationality,
			LocalDateTime targetTime) {
		PricingRule pricingRule = baseMapper.selectMatchedRule(eventId, memberTypeId, nationality, targetTime);
		if (pricingRule == null) {
			throw new PricingRuleException("沒有匹配的價格規則。");
		}
		return pricingRule;
	}

	/**
	 * 核心校驗邏輯
	 * 
	 * @param entity    待檢查的實體
	 * @param excludeId 需要排除的 ID (新增時為 null，更新時為當前 ID)
	 */
	private void validateOverlap(PricingRule entity, Long excludeId) {
		LambdaQueryWrapper<PricingRule> wrapper = new LambdaQueryWrapper<>();

		// 1. 基本維度過濾
		wrapper.eq(PricingRule::getEventId, entity.getEventId())
				.eq(PricingRule::getNationality, entity.getNationality());

		// 2. 處理 member_type_id 為 NULL 的比較
		if (entity.getMemberTypeId() == null) {
			wrapper.isNull(PricingRule::getMemberTypeId);
		} else {
			wrapper.eq(PricingRule::getMemberTypeId, entity.getMemberTypeId());
		}

		// 3. 如果是更新，排除自身 ID
		if (excludeId != null) {
			wrapper.ne(PricingRule::getPricingRuleId, excludeId);
		}

		/**
		 * 時間重疊判斷
		 * 
		 * 舊規則[A,B],新規則[C,D]
		 * 什麼時候「不重疊」？只有兩種情況他們完全沒交集：
		 * 情況一： 新規則整個在舊規則「之前」結束（D < A）。
		 * 情況二： 新規則整個在舊規則「之後」開始（C > B）。
		 * 
		 * 什麼時候「會重疊」？只要排除掉上面那兩種「不重疊」的情況，
		 * 剩下的就全是「重疊」了。根據邏輯判斷的「摩根定律」，把上面不重疊的條件反過來，就是重疊的條件：
		 * 不是 (D < A) -> 變成 D >= A (傳入結束日 >= 資料庫開始日)
		 * 不是 (C > B) -> 變成 C <= B (傳入開始日 <= 資料庫結束日)
		 */
		wrapper.and(w -> w.le(PricingRule::getDateFrom, entity.getDateTo())
				.ge(PricingRule::getDateTo, entity.getDateFrom()));

		// 5. 執行檢查
		if (this.count(wrapper) > 0) {
			throw new PricingRuleException("該時段已存在重疊的價格規則。");
		}
	}

	@Override
	public PricingRule create(AddPricingRuleDTO addPricingRuleDTO) {
		PricingRule pricingRule = pricingRuleConvert.addDTOToEntity(addPricingRuleDTO);

		// 校驗規則是否重複，新增不需排除 ID
		validateOverlap(pricingRule, null);
		baseMapper.insert(pricingRule);
		return pricingRule;
	}

	@Override
	public void update(PutPricingRuleDTO putPricingRuleDTO) {
		PricingRule pricingRule = pricingRuleConvert.putDTOToEntity(putPricingRuleDTO);
		// 校驗規則是否重複，更新必須排除自身 ID，避免與舊資料衝突
		validateOverlap(pricingRule, pricingRule.getPricingRuleId());
		baseMapper.updateById(pricingRule);
	}

	@Override
	public void remove(Long pricingRuleId) {
		baseMapper.deleteById(pricingRuleId);
	}

	@Override
	public void removeByEventId(Long eventId) {
		baseMapper.deleteByEventId(eventId);
		
	}


}
