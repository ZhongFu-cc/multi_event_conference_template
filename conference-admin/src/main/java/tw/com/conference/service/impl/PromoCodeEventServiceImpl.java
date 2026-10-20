package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import tw.com.conference.mapper.PromoCodeEventMapper;
import tw.com.conference.pojo.entity.PromoCodeEvent;
import tw.com.conference.service.PromoCodeEventService;

@Service
@RequiredArgsConstructor
public class PromoCodeEventServiceImpl extends ServiceImpl<PromoCodeEventMapper, PromoCodeEvent>
		implements PromoCodeEventService {

	@Override
	public Set<Long> findEventIdsByCampaignId(Long promoCampaignId) {
		return baseMapper.selectByCampaignId(promoCampaignId)
				.stream()
				.map(PromoCodeEvent::getEventId)
				.collect(Collectors.toSet());
	}

	@Override
	public List<PromoCodeEvent> findByCampaignId(Long promoCampaignId) {
		return baseMapper.selectByCampaignId(promoCampaignId);
	}

	@Override
	public void addEventsToCampaign(Long promoCampaignId, Collection<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return;
		}
		List<PromoCodeEvent> list = eventIds.stream().map(eventId -> {
			PromoCodeEvent promoCodeEvent = new PromoCodeEvent();
			promoCodeEvent.setPromoCampaignId(promoCampaignId);
			promoCodeEvent.setEventId(eventId);
			return promoCodeEvent;
		}).toList();
		this.saveBatch(list);
	}

	@Override
	public void removeEventsFromCampaign(Long promoCampaignId, Collection<Long> eventIds) {
		baseMapper.deleteByCampaignIdAndEventIds(promoCampaignId, eventIds);
	}

	@Override
	public void removeByCampaignId(Long promoCampaignId) {
		baseMapper.deleteByCampaignId(promoCampaignId);
	}

	@Override
	public void removeByEventId(Long eventId) {
		baseMapper.deleteByEventId(eventId);
	}

}
