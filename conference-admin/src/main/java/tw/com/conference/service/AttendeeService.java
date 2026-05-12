package tw.com.conference.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;

/**
 * <p>
 * 參加者表，在註冊並實際繳完註冊費後，會進入這張表中，用做之後發送QRcdoe使用 服务类
 * </p>
 *
 * @author Joey
 * @since 2025-04-24
 */
public interface AttendeeService extends IService<Attendee> {

	/**
	 * 拿到與會者分組 index
	 * 
	 * @param groupSize
	 * @return
	 */
	int getAttendeesGroupIndex(int groupSize);

	Attendee getAttendees(Long attendeesId);
	
	Attendee getAttendeesByMemberId(Long memberId);

	List<Attendee> getAttendeesList();

	/**
	 * mybatis 原始高速查詢所有Attendees<br>
	 * 輸出Excel數據適用
	 * 
	 * @return
	 */
	List<Attendee> getAttendeesEfficiently();

	/**
	 * 根據ids查詢符合的與會者
	 * 
	 * @param attendeesIds
	 * @return
	 */
	List<Attendee> getAttendeesListByIds(Collection<Long> attendeesIds);

	IPage<Attendee> getAttendeesPage(Page<Attendee> page);

	/**
	 * 查詢符合memberList範圍內的與會者
	 * 
	 * @param page
	 * @param memberList
	 * @return
	 */
	IPage<Attendee> getAttendeesPageByMemberList(Page<Attendee> page, Collection<Member> memberList);

	/**
	 * 根據會員資訊 建立 與會者
	 * 
	 * @param member
	 * @return
	 */
	Attendee addAttendees(Member member);

	void deleteAttendees(Long attendeesId);

	/**
	 * 根據memberId 刪除與會者身分
	 * 
	 * @param memberId
	 */
	Attendee deleteAttendeesByMemberId(Long memberId);

	/**
	 * 高效獲取所有與會者的映射對象
	 * 
	 * @return 以attendeesId為key , Attendee 為值的value
	 */
	Map<Long, Attendee> getAttendeesMap();

	/**
	 * 查詢應簽到人數
	 * 
	 * @return
	 */
	Integer countTotalShouldAttend();

}
