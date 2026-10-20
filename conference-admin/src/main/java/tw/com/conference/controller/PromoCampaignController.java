package tw.com.conference.controller;

import java.io.IOException;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.manager.PromoCampaignManager;
import tw.com.conference.pojo.DTO.AssignEvent2PromoCampaignDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddPromoCampaignDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPromoCampaignDTO;
import tw.com.conference.pojo.VO.PromoCampaignVO;
import tw.com.conference.pojo.VO.PromoCodeVO;
import tw.com.conference.service.PromoCampaignService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 優惠碼活動 前端控制器 (後台管理用)
 * </p>
 */
@Tag(name = "優惠碼API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promo-campaign")
public class PromoCampaignController {

	private final PromoCampaignService promoCampaignService;
	private final PromoCampaignManager promoCampaignManager;

	@GetMapping
	@Operation(summary = "查詢所有優惠碼活動 (含碼的使用統計與適用活動)")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<List<PromoCampaignVO>> listPromoCampaign() {
		return R.ok(promoCampaignManager.getCampaignVOList());
	}

	@GetMapping("{id}")
	@Operation(summary = "查詢單一優惠碼活動")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<PromoCampaignVO> getPromoCampaign(@PathVariable("id") Long promoCampaignId) {
		return R.ok(promoCampaignManager.getCampaignVO(promoCampaignId));
	}

	@PostMapping
	@Operation(summary = "新增優惠碼活動", description = "codeType=unique 時以 quantity 指定要產生幾組一次性碼; "
			+ "codeType=general 只產生 1 組可重複使用的碼, 可用 code 自訂字串。"
			+ "折扣型態 all_amount/all_percent 為整張訂單折抵, 不可指定 eventIds")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<PromoCampaignVO> savePromoCampaign(@RequestBody @Valid AddPromoCampaignDTO addPromoCampaignDTO) {
		return R.ok(promoCampaignManager.createCampaign(addPromoCampaignDTO));
	}

	@PutMapping
	@Operation(summary = "修改優惠碼活動", description = "僅能改名稱、可使用期間、啟用狀態。"
			+ "折扣型態與折扣值刻意不開放修改 —— 碼發出去後其折扣語意不該再變動")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updatePromoCampaign(@RequestBody @Valid PutPromoCampaignDTO putPromoCampaignDTO) {
		promoCampaignService.update(putPromoCampaignDTO);
		return R.ok();
	}

	@PutMapping("events")
	@Operation(summary = "為優惠碼活動 分配/移除 適用的活動事件", description = "傳空列表代表改為全場適用。"
			+ "折扣型態為 all_amount/all_percent 時會被拒絕, 因為那兩種忽略適用範圍")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> assignEvent2PromoCampaign(@RequestBody @Valid AssignEvent2PromoCampaignDTO dto) {
		promoCampaignManager.assignEvent2Campaign(dto);
		return R.ok();
	}

	@GetMapping("{id}/codes")
	@Operation(summary = "查詢某優惠碼活動底下所有的碼及其使用情形")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<List<PromoCodeVO>> listPromoCode(@PathVariable("id") Long promoCampaignId) {
		return R.ok(promoCampaignManager.getCodeVOList(promoCampaignId));
	}

	@GetMapping("{id}/codes/download-excel")
	@Operation(summary = "下載優惠碼清單Excel", description = "供後台發送給VIP客戶自行分送; 含使用者與折抵金額")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public void downloadCodesExcel(HttpServletResponse response, @PathVariable("id") Long promoCampaignId)
			throws IOException {
		promoCampaignManager.downloadCodesExcel(response, promoCampaignId);
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除優惠碼活動", description = "連帶刪除其碼與適用活動設定; 使用紀錄會保留作為稽核資料")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deletePromoCampaign(@PathVariable("id") Long promoCampaignId) {
		promoCampaignManager.removeCampaign(promoCampaignId);
		return R.ok();
	}

}
