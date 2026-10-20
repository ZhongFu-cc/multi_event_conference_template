package tw.com.conference.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.DTO.addEntityDTO.AddPromoCampaignDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPromoCampaignDTO;
import tw.com.conference.pojo.entity.PromoCampaign;

public interface PromoCampaignService extends IService<PromoCampaign> {

	PromoCampaign get(Long promoCampaignId);

	List<PromoCampaign> list();

	/**
	 * 建立優惠碼活動本身 (不含碼與適用活動，由 PromoCampaignManager 編排)<br>
	 * 會檢查「折扣型態」與「折扣值」的交叉條件
	 *
	 * @param addPromoCampaignDTO
	 * @return
	 */
	PromoCampaign create(AddPromoCampaignDTO addPromoCampaignDTO);

	/**
	 * 修改優惠碼活動<br>
	 * 僅能改名稱、期間、啟用狀態；折扣型態與折扣值不開放修改
	 *
	 * @param putPromoCampaignDTO
	 */
	void update(PutPromoCampaignDTO putPromoCampaignDTO);

	void remove(Long promoCampaignId);

}
