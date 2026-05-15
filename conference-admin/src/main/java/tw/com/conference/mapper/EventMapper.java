package tw.com.conference.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.pojo.entity.Event;

/**
 * <p>
 * 活動事件表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
public interface EventMapper extends BaseMapper<Event> {

	/**
	 * 搜尋當前時間可報名的活動
	 * 
	 * @return
	 */
	default List<Event> selectCurrentAvailable() {
		LocalDateTime now = LocalDateTime.now();

		LambdaQueryWrapper<Event> queryWrapper = new LambdaQueryWrapper<>();
		// 基礎條件：必須是啟用的活動
		queryWrapper.eq(Event::getIsActive, CommonStatusEnum.YES);

		// 核心邏輯：(現在在註冊時間內) OR (現在在活動時間內)
		queryWrapper.and(wrapper -> wrapper
				// 條件一：在註冊時間內 (OpenAt <= now AND CloseAt >= now)
				.nested(regWrapper -> regWrapper.le(Event::getRegistrationOpenAt, now)
						.ge(Event::getRegistrationCloseAt, now))
				.or() // 或者
				// 條件二：在活動時間內 (startAt <= now AND endAt >= now)
				.nested(timeWrapper -> timeWrapper.le(Event::getStartAt, now).ge(Event::getEndAt, now)));

		return this.selectList(queryWrapper);
	}

	/**
	 * 判斷 事件 表內是否有任何資料<br>
	 * 使用 MySQL 的 EXISTS 語法，只要找到第一筆就會停止掃描，效能最優。
	 */
	@Select("SELECT EXISTS(SELECT 1 FROM event WHERE is_deleted = 0 LIMIT 1)")
	boolean existAnyEvent();
}
