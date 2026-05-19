package tw.com.conference.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import tw.com.conference.pojo.entity.Attendee;

/**
 * <p>
 * 參加者表，在註冊並實際繳完註冊費後，會進入這張表中，用做之後發送QRcdoe使用 Mapper 接口
 * </p>
 *
 * @author Joey
 * @since 2025-04-24
 */
public interface AttendeeMapper extends BaseMapper<Attendee> {


	@Select("SELECT MAX(sequence_no) FROM attendee")
	Integer selectMaxSequenceNo();

	@Select("SELECT * FROM attendee WHERE is_deleted = 0")
	List<Attendee> selectAttendees();

	/**
	 * 查詢應到人數
	 * 
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM attendee WHERE is_deleted = 0")
	Integer countTotalShouldAttend();
	
}
