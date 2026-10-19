package tw.com.conference.service.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.exception.PromoCodeException;
import tw.com.conference.mapper.PromoCodeMapper;
import tw.com.conference.pojo.entity.PromoCode;
import tw.com.conference.service.PromoCodeService;

@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl extends ServiceImpl<PromoCodeMapper, PromoCode> implements PromoCodeService {

	/** 碼字串使用的字元集，刻意排除易誤認的 0 O 1 I L，方便人工轉抄 */
	private static final char[] CODE_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

	/** 隨機部分的長度 */
	private static final int CODE_RANDOM_LENGTH = 8;

	/** 批次產生時的最大重試次數 (碰撞極罕見，僅作為保險) */
	private static final int MAX_GENERATE_ATTEMPTS = 10;

	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	public PromoCode get(Long promoCodeId) {
		return baseMapper.selectById(promoCodeId);
	}

	@Override
	public PromoCode getByCode(String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		// 碼一律以大寫比對，避免會員輸入小寫而比不到
		return baseMapper.selectByCode(code.trim().toUpperCase());
	}

	@Override
	public List<PromoCode> findByCampaignId(Long promoCampaignId) {
		return baseMapper.selectByCampaignId(promoCampaignId);
	}

	@Override
	@Transactional
	public List<PromoCode> batchGenerate(Long promoCampaignId, String codePrefix, int quantity, int maxUses) {

		if (quantity < 1) {
			throw new PromoCodeException("產生數量至少為 1");
		}

		// 1.先在記憶體內產生不重複的碼，再與 DB 現有的碼比對，兩段都過才寫入
		Set<String> candidates = new HashSet<>();
		int attempts = 0;
		while (candidates.size() < quantity) {
			if (++attempts > quantity * MAX_GENERATE_ATTEMPTS) {
				throw new PromoCodeException("優惠碼產生失敗，請稍後再試或更換前綴");
			}
			candidates.add(this.composeCode(codePrefix));
			// 每湊滿一批就剔除 DB 已存在的，避免最後才發現碰撞
			if (candidates.size() >= quantity) {
				this.findExistingCodes(candidates).forEach(candidates::remove);
			}
		}

		// 2.組裝並批次寫入
		List<PromoCode> promoCodeList = candidates.stream().limit(quantity).map(code -> {
			PromoCode promoCode = new PromoCode();
			promoCode.setPromoCampaignId(promoCampaignId);
			promoCode.setCode(code);
			promoCode.setMaxUses(maxUses);
			promoCode.setUsedCount(0);
			return promoCode;
		}).toList();

		this.saveBatch(promoCodeList);

		return promoCodeList;
	}

	@Override
	public PromoCode createWithCode(Long promoCampaignId, String code, int maxUses) {

		String normalized = code == null ? null : code.trim().toUpperCase();
		if (normalized == null || normalized.isBlank()) {
			throw new PromoCodeException("優惠碼不可為空");
		}

		if (this.getByCode(normalized) != null) {
			throw new PromoCodeException("優惠碼 " + normalized + " 已存在");
		}

		PromoCode promoCode = new PromoCode();
		promoCode.setPromoCampaignId(promoCampaignId);
		promoCode.setCode(normalized);
		promoCode.setMaxUses(maxUses);
		promoCode.setUsedCount(0);
		baseMapper.insert(promoCode);

		return promoCode;
	}

	@Override
	public boolean reserveQuota(Long promoCodeId) {
		// 條件式 UPDATE，影響列數為 1 才算占用成功
		return baseMapper.reserveQuota(promoCodeId) == 1;
	}

	@Override
	public void releaseQuota(Long promoCodeId) {
		baseMapper.releaseQuota(promoCodeId);
	}

	@Override
	public boolean hasQuota(PromoCode promoCode) {
		if (promoCode == null) {
			return false;
		}
		int maxUses = promoCode.getMaxUses() == null ? 0 : promoCode.getMaxUses();
		int usedCount = promoCode.getUsedCount() == null ? 0 : promoCode.getUsedCount();
		// maxUses = 0 代表不限次數
		return maxUses == 0 || usedCount < maxUses;
	}

	@Override
	public void removeByCampaignId(Long promoCampaignId) {
		LambdaQueryWrapper<PromoCode> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(PromoCode::getPromoCampaignId, promoCampaignId);
		baseMapper.delete(queryWrapper);
	}

	@Override
	public List<String> findExistingCodes(Collection<String> codes) {
		if (codes == null || codes.isEmpty()) {
			return Collections.emptyList();
		}
		return baseMapper.selectExistingCodes(codes).stream().map(PromoCode::getCode).toList();
	}

	/**
	 * 組出一組碼字串: 前綴 + "-" + 隨機字元
	 *
	 * @param codePrefix 可為 null 或空白
	 * @return
	 */
	private String composeCode(String codePrefix) {

		List<Character> chars = new ArrayList<>(CODE_RANDOM_LENGTH);
		for (int i = 0; i < CODE_RANDOM_LENGTH; i++) {
			chars.add(CODE_ALPHABET[RANDOM.nextInt(CODE_ALPHABET.length)]);
		}

		StringBuilder builder = new StringBuilder();
		if (codePrefix != null && !codePrefix.isBlank()) {
			builder.append(codePrefix.trim().toUpperCase()).append("-");
		}
		chars.forEach(builder::append);

		return builder.toString();
	}

}
