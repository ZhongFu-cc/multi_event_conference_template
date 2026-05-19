package tw.com.conference.mapper;

import java.util.Collection;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.pojo.entity.AttendeeEvent;

/**
 * <p>
 * 與會者-參加活動 表 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2026-05-18
 */
public interface AttendeeEventMapper extends BaseMapper<AttendeeEvent> {

	/**
	 * 更新付款狀態
	 * 
	 * @param memberId 會員ID
	 * @param eventIds 活動事件IDs
	 */
	default void updatePaymentStatusByMemberAndEvents(Long memberId, Collection<Long> eventIds) {
		LambdaUpdateWrapper<AttendeeEvent> updateWrapper = new LambdaUpdateWrapper<>();

		// 設定更新欄位
		updateWrapper.set(AttendeeEvent::getIsPaid, CommonStatusEnum.YES)
				// 設定條件
				.eq(AttendeeEvent::getMemberId, memberId)
				.in(AttendeeEvent::getEventId, eventIds);

		// 執行更新
		this.update(updateWrapper);
	}

	/**
	 * 查詢符合eventId的總數
	 * 
	 * @param eventId
	 * @return
	 */
	default long countByEventId(Long eventId) {

		LambdaQueryWrapper<AttendeeEvent> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AttendeeEvent::getEventId, eventId);

		return this.selectCount(queryWrapper);

	}
}
