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
import tw.com.conference.manager.CategoryPricingRuleManager;
import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberTypeDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutMemberTypeDTO;
import tw.com.conference.pojo.entity.MemberType;
import tw.com.conference.service.MemberTypeService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 會員身份列表 前端控制器
 * </p>
 *
 * @author Joey
 * @since 2026-05-04
 */
@Tag(name = "會員身份類別API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/member-type")
public class MemberTypeController {

	private final MemberTypeService memberTypeService;
	private final CategoryPricingRuleManager categoryPriceRuleManager;

	@GetMapping("exist-any")
	@Operation(summary = "是否存在任何會員身分類別")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Boolean> existAnyMemberType() {
		boolean existAny = memberTypeService.existAny();
		return R.ok(existAny);
	}

	@GetMapping("{id}")
	@Operation(summary = "查詢單一會員身分類別")
	@SaCheckRole("super-admin")
	public R<MemberType> getMemberType(@PathVariable("id") Long memberTypeId) {
		MemberType memberType = memberTypeService.get(memberTypeId);
		return R.ok(memberType);
	}

	@PostMapping
	@Operation(summary = "新增單一會員身分類別")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> saveMemberType(@RequestBody @Valid AddMemberTypeDTO addMemberTypeDTO) {
		memberTypeService.create(addMemberTypeDTO);
		return R.ok();
	}

	@PutMapping
	@Operation(summary = "更新單一會員身分類別")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updateMemberType(@RequestBody @Valid PutMemberTypeDTO putMemberTypeDTO) {
		memberTypeService.update(putMemberTypeDTO);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除單一會員身分類別")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deleteMemberType(@PathVariable("id") Long memberTypeId) {
		categoryPriceRuleManager.removeCategory(memberTypeId);
		return R.ok();
	}

}
