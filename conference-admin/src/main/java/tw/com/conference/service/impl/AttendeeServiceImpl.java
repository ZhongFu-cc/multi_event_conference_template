package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.mapper.AttendeeMapper;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.service.AttendeeService;

/**
 * <p>
 * 參加者表，在註冊並實際繳完註冊費後，會進入這張表中，用做之後發送QRcdoe使用 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2025-04-24
 */
@Service
@RequiredArgsConstructor
public class AttendeeServiceImpl extends ServiceImpl<AttendeeMapper, Attendee> implements AttendeeService {

	private static final String ATTENDEE_SEQUENCE_KEY = "attendee:sequence_lock";

	@Qualifier("businessRedissonClient")
	private final RedissonClient redissonClient;

	@Override
	public int getAttendeesGroupIndex(int groupSize) {
		Long attendeesCount = baseMapper.selectCount(null);
		return (int) Math.ceil(attendeesCount / (double) groupSize);
	}

	@Override
	public Attendee getAttendees(Long attendeesId) {
		return baseMapper.selectById(attendeesId);
	}
	
	@Override
	public Attendee getAttendeesByMemberId(Long memberId) {
		LambdaQueryWrapper<Attendee> attendeesWrapper = new LambdaQueryWrapper<>();
		attendeesWrapper.eq(Attendee::getMemberId, memberId);
		return baseMapper.selectOne(attendeesWrapper);
	}

	@Override
	public List<Attendee> getAttendeesList() {
		return baseMapper.selectList(null);
	}

	@Override
	public List<Attendee> getAttendeesEfficiently() {
		return baseMapper.selectAttendees();
	}
	
	@Override
	public List<Attendee> getAttendeesListByIds(Collection<Long> attendeesIds) {
		if(attendeesIds.isEmpty()) {
			return Collections.emptyList();
		}
		return baseMapper.selectBatchIds(attendeesIds);
	}

	@Override
	public IPage<Attendee> getAttendeesPage(Page<Attendee> page) {
		return baseMapper.selectPage(page, null);
	}

	@Override
	public IPage<Attendee> getAttendeesPageByMemberList(Page<Attendee> page, Collection<Member> memberList) {
		Set<Long> memberIdSet = memberList.stream().map(Member::getMemberId).collect(Collectors.toSet());
		if (memberIdSet.isEmpty()) {
			return new Page<Attendee>(page.getCurrent(), page.getSize());
		}
		LambdaQueryWrapper<Attendee> attendeesWrapper = new LambdaQueryWrapper<>();
		attendeesWrapper.in(Attendee::getMemberId, memberIdSet);
		return baseMapper.selectPage(page, attendeesWrapper);
	}

	@Override
	public Integer countTotalShouldAttend() {
		return baseMapper.countTotalShouldAttend();
	}

	@Override
	public Attendee addAttendees(Member member) {
		Attendee attendees = new Attendee();
		attendees.setEmail(member.getEmail());
		attendees.setMemberId(member.getMemberId());

		RLock lock = redissonClient.getLock(ATTENDEE_SEQUENCE_KEY);
		boolean isLocked = false;

		try {
			// 10秒鐘內不斷嘗試獲取鎖，20秒後必定釋放鎖
			isLocked = lock.tryLock(10, 20, TimeUnit.SECONDS);

			if (isLocked) {
				// 鎖內查一次最大 sequence_no
				Integer lockedMax = baseMapper.selectMaxSequenceNo();
				int nextSeq = (lockedMax != null) ? lockedMax + 1 : 1;

				// 如果 設定城當前最大sequence_no
				attendees.setSequenceNo(nextSeq);
				baseMapper.insert(attendees);
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (isLocked) {
				lock.unlock();
			}

		}

		// 7.返回主鍵ID
		return attendees;
	}

	@Override
	public void deleteAttendees(Long attendeesId) {
		baseMapper.deleteById(attendeesId);
	}

	@Override
	public Attendee deleteAttendeesByMemberId(Long memberId) {

		// 1.根據memberId 查詢attendees
		LambdaQueryWrapper<Attendee> attendeesWrapper = new LambdaQueryWrapper<>();
		attendeesWrapper.eq(Attendee::getMemberId, memberId);
		Attendee attendees = baseMapper.selectOne(attendeesWrapper);

		// 2.如果不為null，刪除attendees
	    if (attendees != null) {
			baseMapper.deleteById(attendees);
	    }
		
		// 3.返回被刪除的與會者，可能為null
		return attendees;

	}

	@Override
	public Map<Long, Attendee> getAttendeesMap() {
		List<Attendee> attendeesList = this.getAttendeesEfficiently();
		return attendeesList.stream().collect(Collectors.toMap(Attendee::getAttendeesId, Function.identity()));
	}





}
