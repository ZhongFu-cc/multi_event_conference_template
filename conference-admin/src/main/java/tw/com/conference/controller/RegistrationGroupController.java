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

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tw.com.conference.manager.RegistrationGroupManager;
import tw.com.conference.pojo.DTO.JoinRegistrationGroupDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddRegistrationGroupDTO;
import tw.com.conference.pojo.VO.GroupInvitePreviewVO;
import tw.com.conference.pojo.VO.GroupOrderVO;
import tw.com.conference.pojo.VO.RegistrationGroupVO;
import tw.com.conference.pojo.entity.GroupDiscountRule;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.saToken.StpKit;
import tw.com.conference.service.MemberService;
import tw.com.conference.utils.R;

/**
 * <p>
 * 團體報名 (邀請碼機制) 前端控制器<br>
 * 負責人建立團體 → 分享邀請碼 → 成員以自己的帳號加入 → 達門檻後負責人鎖定名單並付款
 * </p>
 */
@Tag(name = "團體報名API")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/registration-group")
public class RegistrationGroupController {

	private final MemberService memberService;
	private final RegistrationGroupManager registrationGroupManager;

	@GetMapping("rules")
	@Operation(summary = "查詢某場活動啟用中的團體優惠規則", description = "依最低人數由小到大; discountValue 為減免百分比, 10 表示 9 折")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<List<GroupDiscountRule>> listActiveRules(@RequestParam("eventId") Long eventId) {
		return R.ok(registrationGroupManager.findActiveRules(eventId));
	}

	@PostMapping
	@Operation(summary = "建立團體", description = "建立者即為負責人, 一定出席且計入人數; 一團只能報名一場活動。團體名稱由系統自動產生")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<RegistrationGroupVO> createGroup(@RequestBody @Valid AddRegistrationGroupDTO dto) {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.createGroup(memberCache, dto));
	}

	@GetMapping("mine")
	@Operation(summary = "查詢我參與的所有團體 (擔任負責人或成員)")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<List<RegistrationGroupVO>> listMyGroups() {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.findMyGroups(memberCache));
	}

	@GetMapping("{id}")
	@Operation(summary = "查詢單一團體", description = "需為該團成員; 只有負責人看得到邀請碼、訂單ID與成員名單")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<RegistrationGroupVO> getGroup(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.getGroup(memberCache, registrationGroupId));
	}

	@GetMapping("invite/{inviteCode}")
	@Operation(summary = "以邀請碼查看團體概況 (確認加入前)", description = "不含成員名單")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<GroupInvitePreviewVO> previewInvite(@PathVariable("inviteCode") String inviteCode) {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.previewInvite(memberCache, inviteCode));
	}

	@PostMapping("join")
	@Operation(summary = "以邀請碼加入團體", description = "僅招募中的團體可加入; 已報名此活動或已在此活動其他團體者不可加入")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<RegistrationGroupVO> joinGroup(@RequestBody @Valid JoinRegistrationGroupDTO dto) {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.joinGroup(memberCache, dto));
	}

	@DeleteMapping("{id}/leave")
	@Operation(summary = "退出團體", description = "僅招募中; 負責人不可退出, 請改為解散團體")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<Void> leaveGroup(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		registrationGroupManager.leaveGroup(memberCache, registrationGroupId);
		return R.ok();
	}

	@DeleteMapping("{id}/members/{memberId}")
	@Operation(summary = "負責人移除成員", description = "僅招募中")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<Void> removeMember(@PathVariable("id") Long registrationGroupId,
			@PathVariable("memberId") Long memberId) {
		Member memberCache = memberService.getMemberInfo();
		registrationGroupManager.removeMember(memberCache, registrationGroupId, memberId);
		return R.ok();
	}

	@PutMapping("{id}/invite-code")
	@Operation(summary = "負責人重新產生邀請碼", description = "僅招募中; 舊碼立即失效")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<String> regenerateInviteCode(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		String inviteCode = registrationGroupManager.regenerateInviteCode(memberCache, registrationGroupId);
		// 明確帶 msg，避免 R.ok(String) 被當成訊息而非資料
		return R.ok("操作成功", inviteCode);
	}

	@GetMapping("{id}/preview")
	@Operation(summary = "負責人試算團體訂單金額", description = "不寫入任何資料; 未達門檻時 eligible=false, 不會拋錯")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<GroupOrderVO> previewOrder(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.previewOrder(memberCache, registrationGroupId));
	}

	@PostMapping("{id}/lock")
	@Operation(summary = "負責人鎖定名單並建立團體訂單", description = "人數未達最低門檻會被擋下; 訂單由負責人付款, "
			+ "付款成功後所有成員一起完成報名。不可使用優惠碼與活動組合優惠")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<GroupOrderVO> lockGroup(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		return R.ok(registrationGroupManager.lockGroup(memberCache, registrationGroupId));
	}

	@PostMapping("{id}/unlock")
	@Operation(summary = "負責人解除鎖定", description = "僅已鎖定且尚未付款; 會刪除團體訂單, 團體退回招募中")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<Void> unlockGroup(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		registrationGroupManager.unlockGroup(memberCache, registrationGroupId);
		return R.ok();
	}

	@DeleteMapping("{id}")
	@Operation(summary = "負責人解散團體", description = "僅招募中; 已鎖定需先解除鎖定")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckLogin(type = StpKit.MEMBER_TYPE)
	public R<Void> dissolveGroup(@PathVariable("id") Long registrationGroupId) {
		Member memberCache = memberService.getMemberInfo();
		registrationGroupManager.dissolveGroup(memberCache, registrationGroupId);
		return R.ok();
	}

	/** ------------------- 以下為後台管理用 ------------------------------------ */

	@GetMapping("admin")
	@Operation(summary = "後台查詢某場活動的所有團體及完整成員名單")
	@Parameters({
			@Parameter(name = "Authorization", description = "請求頭token,token-value開頭必須為Bearer ", required = true, in = ParameterIn.HEADER) })
	@SaCheckRole("super-admin")
	public R<List<RegistrationGroupVO>> listGroupsByEvent(@RequestParam("eventId") Long eventId) {
		return R.ok(registrationGroupManager.findGroupsByEvent(eventId));
	}

}
