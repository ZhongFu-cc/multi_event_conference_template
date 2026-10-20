package tw.com.conference.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.PromoCampaignConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.enums.PromoDiscountTypeEnum;
import tw.com.conference.exception.PromoCodeException;
import tw.com.conference.mapper.PromoCampaignMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddPromoCampaignDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPromoCampaignDTO;
import tw.com.conference.pojo.entity.PromoCampaign;
import tw.com.conference.service.PromoCampaignService;

@Service
@RequiredArgsConstructor
public class PromoCampaignServiceImpl extends ServiceImpl<PromoCampaignMapper, PromoCampaign>
		implements PromoCampaignService {

	private static final BigDecimal MAX_PERCENT = new BigDecimal(100);

	private final PromoCampaignConvert promoCampaignConvert;

	@Override
	public PromoCampaign get(Long promoCampaignId) {
		return baseMapper.selectById(promoCampaignId);
	}

	@Override
	public List<PromoCampaign> list() {
		return baseMapper.selectAll();
	}

	@Override
	public PromoCampaign create(AddPromoCampaignDTO addPromoCampaignDTO) {

		this.validateDiscount(addPromoCampaignDTO.getDiscountType(), addPromoCampaignDTO.getDiscountValue());
		this.validateDateRange(addPromoCampaignDTO);

		PromoCampaign promoCampaign = promoCampaignConvert.addDTOToEntity(addPromoCampaignDTO);

		// FREE 型態不使用折扣值，一律存 null 避免誤用
		if (PromoDiscountTypeEnum.FREE.equals(promoCampaign.getDiscountType())) {
			promoCampaign.setDiscountValue(null);
		}

		// 未指定啟用狀態時預設啟用
		if (promoCampaign.getIsActive() == null) {
			promoCampaign.setIsActive(CommonStatusEnum.YES);
		}

		baseMapper.insert(promoCampaign);

		return promoCampaign;
	}

	@Override
	public void update(PutPromoCampaignDTO putPromoCampaignDTO) {

		PromoCampaign existing = this.get(putPromoCampaignDTO.getPromoCampaignId());
		if (existing == null) {
			throw new PromoCodeException("優惠碼活動不存在");
		}

		if (putPromoCampaignDTO.getDateFrom() != null && putPromoCampaignDTO.getDateTo() != null
				&& putPromoCampaignDTO.getDateFrom().isAfter(putPromoCampaignDTO.getDateTo())) {
			throw new PromoCodeException("可使用起始日不可晚於結束日");
		}

		// 只更新開放修改的欄位; codeType / discountType / discountValue 刻意不動,
		// 碼發出去之後折扣語意不該再改變
		PromoCampaign promoCampaign = promoCampaignConvert.putDTOToEntity(putPromoCampaignDTO);
		baseMapper.updateById(promoCampaign);
	}

	@Override
	public void remove(Long promoCampaignId) {
		baseMapper.deleteById(promoCampaignId);
	}

	/**
	 * 檢查折扣型態與折扣值的交叉條件
	 *
	 * @param discountType
	 * @param discountValue
	 */
	private void validateDiscount(PromoDiscountTypeEnum discountType, BigDecimal discountValue) {

		if (!discountType.isDiscountValueRequired()) {
			// FREE 型態不需要折扣值
			return;
		}

		if (discountValue == null) {
			throw new PromoCodeException(discountType.getLabel() + " 必須填寫折扣值");
		}

		if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
			throw new PromoCodeException("折扣值必須大於 0");
		}

		if (discountType.isPercentBased() && discountValue.compareTo(MAX_PERCENT) > 0) {
			throw new PromoCodeException("百分比折扣的折扣值不可超過 100");
		}
	}

	/**
	 * 檢查可使用期間
	 *
	 * @param addPromoCampaignDTO
	 */
	private void validateDateRange(AddPromoCampaignDTO addPromoCampaignDTO) {
		if (addPromoCampaignDTO.getDateFrom() != null && addPromoCampaignDTO.getDateTo() != null
				&& addPromoCampaignDTO.getDateFrom().isAfter(addPromoCampaignDTO.getDateTo())) {
			throw new PromoCodeException("可使用起始日不可晚於結束日");
		}
	}

}
