package tw.com.conference.manager;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.service.EventService;

@Component
@RequiredArgsConstructor
public class EventPriceRuleManager {

	private final EventService eventService;
	
	public void removeEvent(Long eventId) {
		
		// 刪除事件活動 相關的 組合優惠
		
		// 刪除事件活動 相關的 價格策略
		
		// 刪除事件活動 本身
		eventService.remove(eventId);
	}
	
}
