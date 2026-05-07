package tw.com.conference.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.DiscountPackageConvert;
import tw.com.conference.mapper.DiscountPackageMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageDTO;
import tw.com.conference.pojo.entity.DiscountPackage;
import tw.com.conference.service.DiscountPackageService;

/**
 * <p>
 * 活動優惠組合表 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Service
@RequiredArgsConstructor
public class DiscountPackageServiceImpl extends ServiceImpl<DiscountPackageMapper, DiscountPackage>
		implements DiscountPackageService {

	private final DiscountPackageConvert discountPackageConvert;

	@Override
	public DiscountPackage get(Long discountPackageId) {
		return baseMapper.selectById(discountPackageId);
	}

	@Override
	public List<DiscountPackage> list() {
		return baseMapper.selectList(null);
	}

	@Override
	public DiscountPackage create(AddDiscountPackageDTO addDiscountPackageDTO) {
		DiscountPackage discountPackage = discountPackageConvert.addDTOToEntity(addDiscountPackageDTO);
		baseMapper.insert(discountPackage);
		return discountPackage;
	}

	@Override
	public void update(PutDiscountPackageDTO putDiscountPackageDTO) {
		DiscountPackage discountPackage = discountPackageConvert.putDTOToEntity(putDiscountPackageDTO);
		baseMapper.updateById(discountPackage);
	}

	@Override
	public void remove(Long discountPackageId) {
		baseMapper.deleteById(discountPackageId);
	}

}
