package tw.com.conference.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.convert.EventConvert;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.exception.EventException;
import tw.com.conference.mapper.EventMapper;
import tw.com.conference.pojo.DTO.addEntityDTO.AddEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutEventDTO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.service.EventService;

/**
 * <p>
 * 活動事件表 服务实现类
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Service
@RequiredArgsConstructor
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

	private final EventConvert eventConvert;

	@Override
	public boolean existAny() {
		return baseMapper.existAnyEvent();
	}

	@Override
	public long count() {
		return baseMapper.selectCount(null);
	}

	@Override
	public Event get(Long eventId) {
		return baseMapper.selectById(eventId);
	}

	@Override
	public Event getMain() {
		return baseMapper.selectMain();
	}

	@Override
	public List<Event> findAvailable() {
		return baseMapper.selectCurrentAvailable();
	}

	@Override
	public Event create(AddEventDTO addEventDTO) {
		Event event = eventConvert.addDTOToEntity(addEventDTO);
		if (event.getIsMain().equals(CommonStatusEnum.YES)) {
			// 查看看是否已存在主活動，如果有則拋出異常
			if (getMain() != null) {
				throw new EventException("主會議只能有一場");
			}
		}
		baseMapper.insert(event);
		return event;
	}

	@Override
	public void update(PutEventDTO putEventDTO) {
		Event event = eventConvert.putDTOToEntity(putEventDTO);
		baseMapper.updateById(event);

	}

	@Override
	public void remove(Long eventId) {
		baseMapper.deleteById(eventId);
	}

}
