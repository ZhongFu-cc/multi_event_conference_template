package tw.com.conference.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.AttendeeConvert;
import tw.com.conference.pojo.VO.AttendeeVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.MemberService;

@Component
@RequiredArgsConstructor
public class AttendeeVOHandler {

	private final MemberService memberService;
	private final AttendeeService attendeeService;
	private final AttendeeConvert attendeeConvert;

	/**
	 * 根據 attendeeId 獲取 與會者完整資訊
	 * 
	 * @param attendeeId
	 * @return
	 */
	public AttendeeVO getAttendeeVO(Long attendeeId) {
		// 1.查詢到與會者資訊
		Attendee attendee = attendeeService.getAttendee(attendeeId);
		// 2.查詢此與會者的基本資料
		Member member = memberService.getMember(attendee.getMemberId());
		// 3.attendee 轉換成 VO
		AttendeeVO attendeeVO = attendeeConvert.entityToVO(attendee);
		// 4.獲取是否為往年與會者		
//		Boolean existsAttendeesHistory = attendeeHistoryService.existsAttendeesHistory(LocalDate.now().getYear() - 1,
//				member.getIdCard(), member.getEmail());

		// 5.組裝VO
		attendeeVO.setMember(member);
//		attendeeVO.setIsLastYearAttendee(existsAttendeesHistory);

		return attendeeVO;
	}

	/**
	 * 根據與會者列表,獲取AttendeesVO列表
	 * 
	 * @param attendeeList
	 * @return
	 */
	public List<AttendeeVO> getAttendeeVOsByAttendeeList(Collection<Attendee> attendeeList) {

		// 1.根據與會者列表對應的memberId 整合成List,並拿到memberList 
		Map<Long, Member> memberMap = memberService.getMemberMapByAttendeeList(attendeeList);

		// 2.最後組裝成AttendeesVO列表
		List<AttendeeVO> attendeeVOList = attendeeList.stream().map(attendee -> {
			AttendeeVO vo = attendeeConvert.entityToVO(attendee);
			vo.setMember(memberMap.get(attendee.getMemberId()));
			return vo;
		}).collect(Collectors.toList());

		return attendeeVOList;

	}

	/**
	 * 根據與會者ID列表,獲取AttendeesVO列表
	 * 
	 * @param ids
	 * @return
	 */
	public List<AttendeeVO> getAttendeesVOsByAttendeesIds(Collection<Long> ids) {
		// 1.根據ids 查詢與會者列表
		List<Attendee> attendeeList = attendeeService.getAttendeeListByIds(ids);

		// 2.根據與會者列表對應的memberId 整合成List,並拿到memberList 
		Map<Long, Member> memberMap = memberService.getMemberMapByAttendeeList(attendeeList);

		// 最後組裝成AttendeesVO列表
		List<AttendeeVO> attendeeVOList = attendeeList.stream().map(attendee -> {
			AttendeeVO vo = attendeeConvert.entityToVO(attendee);
			vo.setMember(memberMap.get(attendee.getMemberId()));
			return vo;
		}).collect(Collectors.toList());

		return attendeeVOList;
	};

}
