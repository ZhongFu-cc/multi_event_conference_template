package tw.com.conference.service.impl;

import tw.com.conference.pojo.DTO.addEntityDTO.AddPricingRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPricingRuleDTO;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.convert.PricingRuleConvert;
import tw.com.conference.mapper.PricingRuleMapper;
import tw.com.conference.service.PricingRuleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

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
	public PricingRule create(AddPricingRuleDTO addPricingRuleDTO) {
		PricingRule pricingRule = pricingRuleConvert.addDTOToEntity(addPricingRuleDTO);
		baseMapper.insert(pricingRule);
		return pricingRule;
	}

	@Override
	public void update(PutPricingRuleDTO putPricingRuleDTO) {
		PricingRule pricingRule = pricingRuleConvert.putDTOToEntity(putPricingRuleDTO);
		baseMapper.updateById(pricingRule);
	}

	@Override
	public void remove(Long pricingRuleId) {
		baseMapper.deleteById(pricingRuleId);
	}
	
}
