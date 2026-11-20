package tw.com.conference.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.SponsorConvert;
import tw.com.conference.enums.SponsorTierEnum;
import tw.com.conference.mapper.SponsorMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddSponsorDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutSponsorDTO;
import tw.com.conference.pojo.VO.SponsorTierGroupVO;
import tw.com.conference.pojo.entity.Sponsor;
import tw.com.conference.service.SponsorService;
import tw.com.conference.utils.S3Util;

/**
 * <p>
 * 贊助商 服务实现类
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SponsorServiceImpl extends ServiceImpl<SponsorMapper, Sponsor> implements SponsorService {

	private static final String PATH = "sponsor/";

	/** 預設縮放比例 */
	private static final int DEFAULT_SCALE = 100;

	/** 預設排序值 */
	private static final int DEFAULT_SORT = 500;

	private final SponsorConvert sponsorConvert;
	private final S3Util s3Util;

	@Value("${spring.cloud.aws.s3.bucketName}")
	private String bucketName;

	@Override
	public Sponsor getSponsor(Long id) {
		return baseMapper.selectById(id);
	}

	@Override
	public List<Sponsor> getAllSponsor() {
		LambdaQueryWrapper<Sponsor> sponsorWrapper = new LambdaQueryWrapper<>();
		sponsorWrapper.orderByAsc(Sponsor::getTier).orderByAsc(Sponsor::getSort).orderByAsc(Sponsor::getSponsorId);
		return baseMapper.selectList(sponsorWrapper);
	}

	@Override
	public List<SponsorTierGroupVO> getSponsorGroupByTier() {

		// 1.查詢全部贊助商 (已依 tier、sort、sponsorId 排序)，並依等級分組，groupingBy 會保留同等級內的排序
		Map<SponsorTierEnum, List<Sponsor>> tierMap = this.getAllSponsor()
				.stream()
				.collect(Collectors.groupingBy(Sponsor::getTier));

		// 2.依 Enum 宣告順序 (鑽石 > 白金 > 金 > 銀 > 銅 > 無級別) 組裝VO，沒有贊助商的等級略過
		List<SponsorTierGroupVO> groupList = new ArrayList<>();
		for (SponsorTierEnum tier : SponsorTierEnum.values()) {
			List<Sponsor> sponsorList = tierMap.get(tier);
			if (sponsorList == null || sponsorList.isEmpty()) {
				continue;
			}

			SponsorTierGroupVO groupVO = new SponsorTierGroupVO();
			groupVO.setTier(tier);
			groupVO.setTierLabel(tier.getLabel());
			groupVO.setSponsorList(sponsorConvert.entityListToVOList(sponsorList));
			groupList.add(groupVO);
		}

		return groupList;
	}

	@Override
	public IPage<Sponsor> getSponsorPage(Page<Sponsor> page, String queryText, SponsorTierEnum tier) {
		LambdaQueryWrapper<Sponsor> sponsorWrapper = new LambdaQueryWrapper<>();
		sponsorWrapper.like(StringUtils.isNotBlank(queryText), Sponsor::getName, queryText)
				.eq(tier != null, Sponsor::getTier, tier)
				.orderByAsc(Sponsor::getTier)
				.orderByAsc(Sponsor::getSort)
				.orderByAsc(Sponsor::getSponsorId);
		return baseMapper.selectPage(page, sponsorWrapper);
	}

	@Override
	public void addSponsor(MultipartFile file, AddSponsorDTO addSponsorDTO) {

		// 1.資料轉換成實體類
		Sponsor sponsor = sponsorConvert.addDTOToEntity(addSponsorDTO);

		// 2.設定預設值
		if (sponsor.getScale() == null) {
			sponsor.setScale(DEFAULT_SCALE);
		}
		if (sponsor.getSort() == null) {
			sponsor.setSort(DEFAULT_SORT);
		}

		// 3.判斷如有檔案，上傳至S3並設定Logo路徑
		if (file != null && !file.isEmpty()) {
			String dbUrl = s3Util.upload(PATH, file.getOriginalFilename(), file);
			sponsor.setLogoUrl(dbUrl);
		}

		// 4.insert 進資料庫
		baseMapper.insert(sponsor);
	}

	@Override
	public void updateSponsor(MultipartFile file, PutSponsorDTO putSponsorDTO) {

		// 1.轉換資料
		Sponsor sponsor = sponsorConvert.putDTOToEntity(putSponsorDTO);

		// 2.判斷是否有檔案，有的話先上傳新Logo，再刪除舊Logo
		String oldLogoUrl = null;
		if (file != null && !file.isEmpty()) {

			// 先找到之前儲存的檔案路徑
			Sponsor currentSponsor = baseMapper.selectById(sponsor.getSponsorId());
			if (currentSponsor != null) {
				oldLogoUrl = currentSponsor.getLogoUrl();
			}

			// 上傳新檔案至S3
			String dbUrl = s3Util.upload(PATH, file.getOriginalFilename(), file);
			sponsor.setLogoUrl(dbUrl);
		}

		// 3.更新贊助商資料
		baseMapper.updateById(sponsor);

		// 4.更新成功後，移除舊的Logo
		this.removeLogo(oldLogoUrl);
	}

	@Override
	public void deleteSponsor(Long id) {

		// 1.先找到之前儲存的檔案路徑
		Sponsor currentSponsor = baseMapper.selectById(id);
		if (currentSponsor == null) {
			return;
		}

		// 2.移除資料庫資料
		baseMapper.deleteById(id);

		// 3.移除S3 Logo
		this.removeLogo(currentSponsor.getLogoUrl());
	}

	@Override
	public void deleteSponsorList(List<Long> ids) {
		for (Long id : ids) {
			this.deleteSponsor(id);
		}
	}

	/**
	 * 移除S3上的Logo，dbUrl 為空則略過
	 *
	 * @param dbUrl 資料庫中儲存的路徑 (/bucketName/xxx)
	 */
	private void removeLogo(String dbUrl) {
		if (StringUtils.isBlank(dbUrl)) {
			return;
		}
		// 去掉 /bucketName/ 這個前墜，才是真正S3儲存的位置
		String s3Key = s3Util.extractS3PathInDbUrl(bucketName, dbUrl);
		s3Util.removeFileIfPresent(bucketName, s3Key);
	}

}
