package tw.com.conference.manager;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.service.EventService;

@Component
@RequiredArgsConstructor
public class SettingManager {

	private final EventService eventService;

	/**
	 * 初始化判斷<br>
	 * @return
	 */
	public boolean isInitialized() {
		
		return false;
		
	}
	
}
