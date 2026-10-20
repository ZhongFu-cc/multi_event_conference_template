package tw.com.conference.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.PromoCampaign;

/**
 * <p>
 * 優惠碼活動 Mapper 接口
 * </p>
 */
public interface PromoCampaignMapper extends BaseMapper<PromoCampaign> {

	/**
	 * 查詢所有優惠碼活動 (新到舊)
	 *
	 * @return
	 */
	default List<PromoCampaign> selectAll() {
		LambdaQueryWrapper<PromoCampaign> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.orderByDesc(PromoCampaign::getPromoCampaignId);
		return this.selectList(queryWrapper);
	}

}
