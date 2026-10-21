package tw.com.conference.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.pojo.DTO.addEntityDTO.AddGroupDiscountRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutGroupDiscountRuleDTO;
import tw.com.conference.pojo.entity.GroupDiscountRule;
import tw.com.conference.service.GroupDiscountRuleService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 團體報名優惠規則 前端控制器 (後台管理用)
 * </p>
 */
@Tag(name = "團體報名優惠規則API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/group-discount-rule")
public class GroupDiscountRuleController {

	private final GroupDiscountRuleService groupDiscountRuleService;

	@GetMapping
	@Operation(summary = "查詢某場活動的所有團體優惠規則 (含停用)")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<List<GroupDiscountRule>> listGroupDiscountRule(@RequestParam("eventId") Long eventId) {
		return R.ok(groupDiscountRuleService.findByEventId(eventId));
	}

	@PostMapping
	@Operation(summary = "新增團體優惠規則", description = "同一場活動可設多條階梯, 最低人數不可重複; "
			+ "discountValue 為減免百分比, 10 表示 9 折")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<GroupDiscountRule> saveGroupDiscountRule(
			@RequestBody @Valid AddGroupDiscountRuleDTO addGroupDiscountRuleDTO) {
		return R.ok(groupDiscountRuleService.create(addGroupDiscountRuleDTO));
	}

	@PutMapping
	@Operation(summary = "修改團體優惠規則", description = "只影響之後才鎖定名單的團體, 已建立的團體訂單金額不變")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updateGroupDiscountRule(@RequestBody @Valid PutGroupDiscountRuleDTO putGroupDiscountRuleDTO) {
		groupDiscountRuleService.update(putGroupDiscountRuleDTO);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除團體優惠規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deleteGroupDiscountRule(@PathVariable("id") Long groupDiscountRuleId) {
		groupDiscountRuleService.remove(groupDiscountRuleId);
		return R.ok();
	}

}
