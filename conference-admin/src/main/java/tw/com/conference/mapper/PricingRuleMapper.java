package tw.com.conference.mapper;

import tw.com.conference.pojo.entity.PricingRule;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 價格規則表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface PricingRuleMapper extends BaseMapper<PricingRule> {

	/**
     * 判斷 價格規則 表內是否有任何資料<br>
     * 使用 MySQL 的 EXISTS 語法，只要找到第一筆就會停止掃描，效能最優。
     */
    @Select("SELECT EXISTS(SELECT 1 FROM event LIMIT 1)")
    boolean existAnyPricingRule();
	
}
