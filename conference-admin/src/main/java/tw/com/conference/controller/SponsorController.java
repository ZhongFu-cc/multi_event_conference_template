package tw.com.conference.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import tw.com.conference.enums.SponsorTierEnum;
import tw.com.conference.pojo.DTO.addEntityDTO.AddSponsorDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutSponsorDTO;
import tw.com.conference.pojo.VO.SponsorTierGroupVO;
import tw.com.conference.pojo.entity.Sponsor;
import tw.com.conference.service.SponsorService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 贊助商 前端控制器
 * </p>
 */
@Tag(name = "贊助商API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/sponsor")
public class SponsorController {

	private final SponsorService sponsorService;

	@GetMapping("{id}")
	@Operation(summary = "查詢單一贊助商")
	public R<Sponsor> getSponsor(@PathVariable("id") Long sponsorId) {
		return R.ok(sponsorService.getSponsor(sponsorId));
	}

	@GetMapping
	@Operation(summary = "查詢全部贊助商(依等級分組)，給前端顯示用", description = "先依等級(鑽石>白金>金>銀>銅>無級別)分組，各等級內再依 sort 排序<br>"
			+ "沒有贊助商的等級不會回傳")
	public R<List<SponsorTierGroupVO>> getSponsorList() {
		return R.ok(sponsorService.getSponsorGroupByTier());
	}

	@GetMapping("pagination")
	@Operation(summary = "查詢全部贊助商(分頁)", description = "tier 可選: diamond, platinum, gold, silver, bronze, none")
	public R<IPage<Sponsor>> getSponsorPage(@RequestParam Integer page, @RequestParam Integer size,
			@RequestParam(required = false) String queryText, @RequestParam(required = false) String tier) {
		Page<Sponsor> pageable = new Page<Sponsor>(page, size);
		SponsorTierEnum tierEnum = tier == null || tier.isBlank() ? null : SponsorTierEnum.fromValue(tier);
		return R.ok(sponsorService.getSponsorPage(pageable, queryText, tierEnum));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "新增贊助商", description = "請使用formData包裝,兩個key <br>" + "1.data(value = DTO(json))<br>"
			+ "2.file(value = binary, Logo圖檔)<br>" + "knife4j Web 文檔顯示有問題, 真實傳輸方式為 「multipart/form-data」<br>"
			+ "請用 http://localhost:8080/swagger-ui/index.html 測試 ")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> saveSponsor(@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart("data") @Schema(name = "data", implementation = AddSponsorDTO.class) String jsonData)
			throws JsonMappingException, JsonProcessingException {

		// 將 JSON 字符串轉為對象
		ObjectMapper objectMapper = new ObjectMapper();
		AddSponsorDTO addSponsorDTO = objectMapper.readValue(jsonData, AddSponsorDTO.class);

		sponsorService.addSponsor(file, addSponsorDTO);
		return R.ok();
	}

	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "修改贊助商", description = "請使用formData包裝,兩個key <br>" + "1.data(value = DTO(json))<br>"
			+ "2.file(value = binary, Logo圖檔, 有傳才會替換)<br>"
			+ "knife4j Web 文檔顯示有問題, 真實傳輸方式為 「multipart/form-data」<br>"
			+ "請用 http://localhost:8080/swagger-ui/index.html 測試 ")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> updateSponsor(@RequestPart(value = "file", required = false) MultipartFile file,
			@RequestPart("data") @Schema(name = "data", implementation = PutSponsorDTO.class) String jsonData)
			throws JsonMappingException, JsonProcessingException {

		// 將 JSON 字符串轉為對象
		ObjectMapper objectMapper = new ObjectMapper();
		PutSponsorDTO putSponsorDTO = objectMapper.readValue(jsonData, PutSponsorDTO.class);

		sponsorService.updateSponsor(file, putSponsorDTO);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "刪除贊助商")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> deleteSponsor(@PathVariable("id") Long sponsorId) {
		sponsorService.deleteSponsor(sponsorId);
		return R.ok();
	}

	@DeleteMapping
	@Operation(summary = "批量刪除贊助商")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<Void> batchDeleteSponsor(@RequestBody List<Long> ids) {
		sponsorService.deleteSponsorList(ids);
		return R.ok();
	}

}
