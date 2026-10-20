package tw.com.conference.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.PromoRedemptionStatusEnum;
import tw.com.conference.mapper.PromoCodeRedemptionMapper;
import tw.com.conference.pojo.entity.PromoCodeRedemption;
import tw.com.conference.service.PromoCodeRedemptionService;

@Service
@RequiredArgsConstructor
public class PromoCodeRedemptionServiceImpl extends ServiceImpl<PromoCodeRedemptionMapper, PromoCodeRedemption>
		implements PromoCodeRedemptionService {

	@Override
	public PromoCodeRedemption getByOrdersId(Long ordersId) {
		return baseMapper.selectByOrdersId(ordersId);
	}

	@Override
	public List<PromoCodeRedemption> findByCampaignId(Long promoCampaignId) {
		return baseMapper.selectByCampaignId(promoCampaignId);
	}

	@Override
	public PromoCodeRedemption createReserved(Long promoCodeId, Long promoCampaignId, Long memberId, Long ordersId,
			BigDecimal discountAmount) {

		PromoCodeRedemption redemption = new PromoCodeRedemption();
		redemption.setPromoCodeId(promoCodeId);
		redemption.setPromoCampaignId(promoCampaignId);
		redemption.setMemberId(memberId);
		redemption.setOrdersId(ordersId);
		redemption.setDiscountAmount(discountAmount);
		redemption.setStatus(PromoRedemptionStatusEnum.RESERVED);
		baseMapper.insert(redemption);

		return redemption;
	}

	@Override
	public void markRedeemed(Long ordersId) {
		baseMapper.markRedeemedByOrdersId(ordersId);
	}

	@Override
	public void removeByOrdersId(Long ordersId) {
		baseMapper.deleteByOrdersId(ordersId);
	}

}
