package tw.com.conference.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import tw.com.conference.manager.CategoryPricingRuleManager;
import tw.com.conference.pojo.DTO.addEntityDTO.AddPricingRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPricingRuleDTO;
import tw.com.conference.pojo.entity.PricingRule;
import tw.com.conference.service.PricingRuleService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 價格規則表 前端控制器
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Tag(name = "價格規則 API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/pricing-rule")
public class PricingRuleController {

	private final PricingRuleService pricingRuleService;
	private final CategoryPricingRuleManager categoryPriceRuleManager;

	@GetMapping("exist-any")
	@Operation(summary = "是否存在任何價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Boolean> existAnyPricingRule() {
		boolean existAny = pricingRuleService.existAny();
		return R.ok(existAny);
	}

	@GetMapping("{id}")
	@Operation(summary = "查詢單一價格規則")
	@SaCheckRole("super-admin")
	public R<PricingRule> getPricingRule(@PathVariable("id") Long pricingRuleId) {
		PricingRule pricingRule = pricingRuleService.get(pricingRuleId);
		return R.ok(pricingRule);
	}

	@PostMapping
	@Operation(summary = "新增單一價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> savePricingRule(@RequestBody @Valid AddPricingRuleDTO addPricingRuleDTO) {
		pricingRuleService.create(addPricingRuleDTO);
		return R.ok();
	}

	@PostMapping
	@Operation(summary = "更新單一價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updatePricingRule(@RequestBody @Valid PutPricingRuleDTO putPricingRuleDTO) {
		pricingRuleService.update(putPricingRuleDTO);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除單一價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deletePricingRule(@PathVariable("id") Long pricingRuleId) {
		categoryPriceRuleManager.removeCategory(pricingRuleId);
		return R.ok();
	}
	
}
