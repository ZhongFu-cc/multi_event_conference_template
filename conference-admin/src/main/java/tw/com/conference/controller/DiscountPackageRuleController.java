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
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageRuleDTO;
import tw.com.conference.pojo.entity.DiscountPackageRule;
import tw.com.conference.service.DiscountPackageRuleService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 活動組合優惠 - 規則 前端控制器
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Tag(name = "活動優惠組合-價格規則API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/discount-package-rule")
public class DiscountPackageRuleController {

	private final DiscountPackageRuleService discountPackageRuleService;

	@GetMapping("{id}")
	@Operation(summary = "查詢單一 優惠組合-價格規則")
	@SaCheckRole("super-admin")
	public R<DiscountPackageRule> getDiscountPackageRule(@PathVariable("id") Long discountPackageRuleId) {
		DiscountPackageRule discountPackage = discountPackageRuleService.get(discountPackageRuleId);
		return R.ok(discountPackage);
	}

	@GetMapping()
	@Operation(summary = "查詢所有 優惠組合-價格規則")
	@SaCheckRole("super-admin")
	public R<List<DiscountPackageRule>> listDiscountPackageRule() {
		List<DiscountPackageRule> list = discountPackageRuleService.list();
		return R.ok(list);
	}

	@PostMapping
	@Operation(summary = "新增單一 組合優惠-價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> saveDiscountPackageRule(@RequestBody @Valid AddDiscountPackageRuleDTO addDiscountPackageRuleDTO) {
		discountPackageRuleService.create(addDiscountPackageRuleDTO);
		return R.ok();
	}

	@PutMapping
	@Operation(summary = "更新單一組合優惠-價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updateDiscountPackageRule(@RequestBody @Valid PutDiscountPackageRuleDTO putDiscountPackageRuleDTO) {
		discountPackageRuleService.update(putDiscountPackageRuleDTO);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除單一組合優惠-價格規則")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deleteDiscountPackageRule(@PathVariable("id") Long discountPackageRuleId) {
		discountPackageRuleService.remove(discountPackageRuleId);
		return R.ok();
	}

}
