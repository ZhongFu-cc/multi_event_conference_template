package tw.com.conference.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.UsageContextEnum;
import tw.com.conference.pojo.entity.MemberType;

/**
 * <p>
 * 會員身份列表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface MemberTypeMapper extends BaseMapper<MemberType> {

	/**
	 * 根據UsageContext查詢
	 * 
	 * @param usageContextEnum
	 * @return
	 */
	default List<MemberType> selectByUsageContext(UsageContextEnum usageContextEnum) {
		LambdaQueryWrapper<MemberType> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(MemberType::getUsageContext, usageContextEnum);
		return this.selectList(queryWrapper);
	}

	/**
	 * 判斷 會員身份 表內是否有任何資料<br>
	 * 使用 MySQL 的 EXISTS 語法，只要找到第一筆就會停止掃描，效能最優。
	 */
	@Select("SELECT EXISTS(SELECT 1 FROM member_type WHERE is_deleted = 0 LIMIT 1)")
	boolean existAnyMemberType();

}
