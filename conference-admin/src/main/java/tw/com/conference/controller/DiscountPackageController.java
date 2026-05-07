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
import tw.com.conference.manager.DiscountPackageManager;
import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageDTO;
import tw.com.conference.pojo.entity.DiscountPackage;
import tw.com.conference.service.DiscountPackageService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 活動優惠組合表 前端控制器
 * </p>
 *
 * @author Joey
 * @since 2026-05-06
 */
@Tag(name = "活動優惠組合API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/discount-package")
public class DiscountPackageController {

	private final DiscountPackageService discountPackageService;
	private final DiscountPackageManager discountPackageManager;

	@GetMapping("{id}")
	@Operation(summary = "查詢單一優惠組合")
	@SaCheckRole("super-admin")
	public R<DiscountPackage> getDiscountPackage(@PathVariable("id") Long discountPackageId) {
		DiscountPackage discountPackage = discountPackageService.get(discountPackageId);
		return R.ok(discountPackage);
	}

	@GetMapping()
	@Operation(summary = "查詢所有優惠組合")
	@SaCheckRole("super-admin")
	public R<List<DiscountPackage>> listDiscountPackage() {
		List<DiscountPackage> list = discountPackageService.list();
		return R.ok(list);
	}

	@PostMapping
	@Operation(summary = "新增單一組合優惠")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> saveDiscountPackage(@RequestBody @Valid AddDiscountPackageDTO addDiscountPackageDTO) {
		discountPackageService.create(addDiscountPackageDTO);
		return R.ok();
	}

	@PutMapping
	@Operation(summary = "更新單一組合優惠")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updateDiscountPackage(@RequestBody @Valid PutDiscountPackageDTO putDiscountPackageDTO) {
		discountPackageService.update(putDiscountPackageDTO);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除單一組合優惠")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deleteDiscountPackage(@PathVariable("id") Long discountPackageId) {
		discountPackageManager.removeDiscountPackage(discountPackageId);
		return R.ok();
	}

}
