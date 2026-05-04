package tw.com.conference.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.manager.EventPriceRuleManager;
import tw.com.conference.pojo.DTO.addEntityDTO.AddEventDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutEventDTO;
import tw.com.conference.pojo.entity.Event;
import tw.com.conference.service.EventService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 活動事件表 前端控制器
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */

@Tag(name = "活動事件API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/event")
public class EventController {

	private final EventPriceRuleManager eventPriceRuleManager;
	private final EventService eventService;
	
	@GetMapping("exist-any")
	@Operation(summary = "是否存在任何活動事件")
	@Parameters({
		@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Boolean> existAnyEvent() {
		boolean existAny = eventService.existAny();
		return R.ok(existAny);
	}
	
	@GetMapping("{id}")
	@Operation(summary = "查詢單一Event")
	@SaCheckRole("super-admin")
	public R<Event> getEvent(@PathVariable("id") Long eventId) {
		Event event = eventService.get(eventId);
		return R.ok(event);
	}
	
	@PostMapping
	@Operation(summary = "新增單一活動事件")
	@Parameters({
		@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> saveEvent(@RequestBody @Valid AddEventDTO addEventDTO)  {
		eventService.create(addEventDTO);
		return R.ok();
	}
	
	@PutMapping
	@Operation(summary = "更新單一活動事件")
	@Parameters({
		@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updateEvent(@RequestBody @Valid PutEventDTO putEventDTO)  {
		eventService.update(putEventDTO);
		return R.ok();
	}
	
	@DeleteMapping("{id}")
	@Operation(summary = "刪除單一活動事件")
	@Parameters({
		@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deleteEvent(@PathVariable("id") Long eventId)  {
		eventPriceRuleManager.removeEvent(eventId);
		return R.ok();
	}

	
}
