package tw.com.conference.service;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import jakarta.validation.Valid;
import tw.com.conference.enums.SponsorTierEnum;
import tw.com.conference.pojo.DTO.addEntityDTO.AddSponsorDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutSponsorDTO;
import tw.com.conference.pojo.VO.SponsorTierGroupVO;
import tw.com.conference.pojo.entity.Sponsor;

/**
 * <p>
 * 贊助商 服务类
 * </p>
 */
@Validated
public interface SponsorService extends IService<Sponsor> {

	/**
	 * 根據ID，獲取sponsor
	 *
	 * @param id
	 * @return
	 */
	Sponsor getSponsor(Long id);

	/**
	 * 獲取所有sponsor (依 tier、sort、sponsorId 排序)
	 *
	 * @return
	 */
	List<Sponsor> getAllSponsor();

	/**
	 * 獲取所有sponsor，並依贊助等級分組 (鑽石 > 白金 > 金 > 銀 > 銅 > 無級別)，沒有贊助商的等級不回傳<br>
	 * 給前端顯示用
	 *
	 * @return
	 */
	List<SponsorTierGroupVO> getSponsorGroupByTier();

	/**
	 * 根據查詢條件，獲取sponsor(分頁)
	 *
	 * @param page      分頁資訊
	 * @param queryText 名稱模糊查詢字串
	 * @param tier      贊助等級
	 * @return
	 */
	IPage<Sponsor> getSponsorPage(Page<Sponsor> page, String queryText, SponsorTierEnum tier);

	/**
	 * 新增 sponsor
	 *
	 * @param file          Logo 圖檔
	 * @param addSponsorDTO 贊助商資訊
	 */
	void addSponsor(MultipartFile file, @Valid AddSponsorDTO addSponsorDTO);

	/**
	 * 更新 sponsor
	 *
	 * @param file          Logo 圖檔，有傳才會替換
	 * @param putSponsorDTO 贊助商資訊
	 */
	void updateSponsor(MultipartFile file, @Valid PutSponsorDTO putSponsorDTO);

	/**
	 * 根據ID，刪除sponsor (連同S3 Logo)
	 *
	 * @param id
	 */
	void deleteSponsor(Long id);

	/**
	 * 根據ID列表，刪除sponsors
	 *
	 * @param ids
	 */
	void deleteSponsorList(List<Long> ids);

}
