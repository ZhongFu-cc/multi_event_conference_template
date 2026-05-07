package tw.com.conference.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.DiscountPackageRuleConvert;
import tw.com.conference.mapper.DiscountPackageRuleMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageRuleDTO;
import tw.com.conference.pojo.entity.DiscountPackageRule;
import tw.com.conference.service.DiscountPackageRuleService;

/**
 * <p>
 * 活動組合優惠 - 規則 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Service
@RequiredArgsConstructor
public class DiscountPackageRuleServiceImpl extends ServiceImpl<DiscountPackageRuleMapper, DiscountPackageRule>
		implements DiscountPackageRuleService {

	private final DiscountPackageRuleConvert discountPackageRuleConvert;

	@Override
	public DiscountPackageRule get(Long discountPackageRuleId) {
		return baseMapper.selectById(discountPackageRuleId);
	}

	@Override
	public DiscountPackageRule create(AddDiscountPackageRuleDTO addDiscountPackageRuleDTO) {
		DiscountPackageRule discountPackageRule = discountPackageRuleConvert.addDTOToEntity(addDiscountPackageRuleDTO);
		baseMapper.insert(discountPackageRule);
		return discountPackageRule;
	}

	@Override
	public void update(PutDiscountPackageRuleDTO putDiscountPackageRuleDTO) {
		DiscountPackageRule discountPackageRule = discountPackageRuleConvert.putDTOToEntity(putDiscountPackageRuleDTO);
		baseMapper.updateById(discountPackageRule);
	}

	@Override
	public void remove(Long discountPackageRuleId) {
		baseMapper.deleteById(discountPackageRuleId); 
	}

	@Override
	public void removeByDiscountPackageId(Long discountPackageId) {
		baseMapper.deleteByDiscountPackageId(discountPackageId);		
	}

}
