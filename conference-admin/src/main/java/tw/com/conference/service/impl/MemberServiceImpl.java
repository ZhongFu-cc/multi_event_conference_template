package tw.com.conference.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.RequiredArgsConstructor;
import tw.com.conference.constants.I18nMessageKey;
import tw.com.conference.convert.MemberConvert;
import tw.com.conference.enums.NationalityEnum;
import tw.com.conference.exception.AccountPasswordWrongException;
import tw.com.conference.exception.ForgetPasswordException;
import tw.com.conference.exception.MemberException;
import tw.com.conference.exception.RegisteredAlreadyExistsException;
import tw.com.conference.helper.MessageHelper;
import tw.com.conference.mapper.MemberMapper;
import tw.com.conference.pojo.DTO.AddGroupMemberDTO;
import tw.com.conference.pojo.DTO.AddMemberForAdminDTO;
import tw.com.conference.pojo.DTO.MemberEmailLogin;
import tw.com.conference.pojo.DTO.MemberIdCardLogin;
import tw.com.conference.pojo.DTO.MemberLoginDTO;
import tw.com.conference.pojo.DTO.WalkInRegistrationDTO;
import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutMemberDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutMemberForAdminDTO;
import tw.com.conference.pojo.VO.MemberTagVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.saToken.StpKit;
import tw.com.conference.service.MemberService;
import tw.com.conference.utils.CountryUtil;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

	private static final String MEMBER_CACHE_INFO_KEY = "memberInfo";
	private final MessageHelper messageHelper;
	private final MemberConvert memberConvert;

	@Override
	public Member getMember(Long memberId) {
		return baseMapper.selectById(memberId);
	}

	@Override
	public String getOnlyMemberName(Member member) {
		String chineseName = member.getChineseName();
		if (StringUtils.isNotBlank(chineseName)) {
			return chineseName;
		}

		String firstName = Optional.ofNullable(member.getFirstName()).orElse("");
		String lastName = Optional.ofNullable(member.getLastName()).orElse("");
		return (firstName + " " + lastName).trim();
	}

	@Override
	public List<Member> getMembersEfficiently() {
		return baseMapper.selectMembers();
	}

	@Override
	public List<Member> getMemberList() {
		List<Member> memberList = baseMapper.selectList(null);
		return memberList;
	}

	@Override
	public List<Member> getMemberListByIds(Collection<Long> memberIds) {

		// 1.如果memberIds為空,返回空數組
		if (memberIds.isEmpty()) {
			return Collections.emptyList();
		}

		// 2.如果有則直接查詢
		return baseMapper.selectBatchIds(memberIds);

	}

	@Override
	public List<Member> getMembersByQuery(String queryText) {
		LambdaQueryWrapper<Member> memberWrapper = new LambdaQueryWrapper<>();

		// 當 queryText 不為空字串、空格字串、Null 時才加入篩選條件
		memberWrapper.and(StringUtils.isNotBlank(queryText),
				wrapper -> wrapper.like(Member::getChineseName, queryText)
						.or()
						.like(Member::getFirstName, queryText)
						.or()
						.like(Member::getLastName, queryText)
						.or()
						.like(Member::getPhone, queryText)
						.or()
						.like(Member::getIdCard, queryText)
						.or()
						.like(Member::getEmail, queryText));

		return baseMapper.selectList(memberWrapper);

	}

	@Override
	public IPage<Member> getMemberPage(Page<Member> page) {
		return baseMapper.selectPage(page, null);
	}

	@Override
	public Long getMemberCount() {
		return baseMapper.selectCount(null);
	}

	@Override
	public Member getMemberByEmail(String email) {
		// 1.透過Email查詢Member
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getEmail, email);

		Member member = baseMapper.selectOne(memberQueryWrapper);
		// 2.如果沒找到該email的member，則直接丟異常給全局處理
		if (member == null) {
			throw new ForgetPasswordException(messageHelper.get(I18nMessageKey.Registration.Auth.EMAIL_NOT_FOUND));
		}

		return member;
	}

	@Override
	public List<Member> getMembersByGroupCodeAndRole(String groupCode, String groupRole) {
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getGroupCode, groupCode).eq(Member::getGroupRole, groupRole);
		return baseMapper.selectList(memberQueryWrapper);
	}

	@Override
	public int getMemberGroupIndex(int groupSize) {
		Long memberCount = baseMapper.selectCount(null);
		return (int) Math.ceil(memberCount / (double) groupSize);
	}

	@Override
	public int getMemberCategoryGroupIndex(int groupSize, Long memberTypeId) {

		LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(Member::getMemberTypeId, memberTypeId);

		Long memberCategoryCount = baseMapper.selectCount(queryWrapper);
		return (int) Math.ceil(memberCategoryCount / (double) groupSize);
	}

	/**
	 * 判斷Email 及 身分證字號 是否被重複註冊,沒有則新增
	 * 
	 * @param member
	 */
	private void validateAndAddMember(Member member) {

		// 判斷email是否重複註冊
		LambdaQueryWrapper<Member> emailQueryWrapper = new LambdaQueryWrapper<>();
		emailQueryWrapper.eq(Member::getEmail, member.getEmail());
		Long emailCount = baseMapper.selectCount(emailQueryWrapper);

		if (emailCount > 0) {
			throw new RegisteredAlreadyExistsException(
					messageHelper.get(I18nMessageKey.Registration.Auth.ACCOUNT_REGISTERED));
		}

		if (member.getIdCard() != null) {
			// 判斷身分證是否重複註冊
			LambdaQueryWrapper<Member> idCardQueryWrapper = new LambdaQueryWrapper<>();
			idCardQueryWrapper.eq(Member::getIdCard, member.getIdCard());
			Long idCardCount = baseMapper.selectCount(idCardQueryWrapper);

			if (idCardCount > 0) {
				throw new RegisteredAlreadyExistsException(
						messageHelper.get(I18nMessageKey.Registration.Auth.ACCOUNT_REGISTERED));
			}
		}

		baseMapper.insert(member);

	}

	@Override
	public Member addMember(AddMemberDTO addMemberDTO) {
		// 1.資料轉換
		Member currentMember = memberConvert.addDTOToEntity(addMemberDTO);
		// 2.使用驗證並新增
		this.validateAndAddMember(currentMember);
		// 3.返回Member資料
		return currentMember;
	}

	@Override
	public Member addMemberForAdmin(AddMemberForAdminDTO addMemberForAdminDTO) {
		// 1.資料轉換
		Member currentMember = memberConvert.forAdminAddDTOToEntity(addMemberForAdminDTO);
		// 2.使用驗證並新增
		this.validateAndAddMember(currentMember);
		// 3.返回Member資料
		return currentMember;

	}

	@Override
	public Member addMemberByRoleAndGroup(String groupCode, String groupRole, AddGroupMemberDTO addGroupMemberDTO) {
		Member member = memberConvert.addGroupDTOToEntity(addGroupMemberDTO);
		member.setGroupCode(groupCode);
		member.setGroupRole(groupRole);
		baseMapper.insert(member);
		return member;
	}

	/**
	 * 現場報到時，新增會員
	 * 
	 * @param walkInRegistrationDTO
	 * @return
	 */
	@Override
	public Member addMemberOnSite(WalkInRegistrationDTO walkInRegistrationDTO) {

		Member member = new Member();
		member.setEmail(walkInRegistrationDTO.getEmail());
		member.setChineseName(walkInRegistrationDTO.getChineseName());
		member.setFirstName(walkInRegistrationDTO.getFirstName());
		member.setLastName(walkInRegistrationDTO.getLastName());
		member.setMemberTypeId(walkInRegistrationDTO.getMemberTypeId());
		member.setCountry(walkInRegistrationDTO.getCountry());

		//判斷Email有無被註冊過
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getEmail, member.getEmail());
		Long memberCount = baseMapper.selectCount(memberQueryWrapper);

		if (memberCount > 0) {
			throw new RegisteredAlreadyExistsException(
					messageHelper.get(I18nMessageKey.Registration.Auth.EMAIL_REGISTERED));
		}

		baseMapper.insert(member);

		return member;
	};

	@Override
	public void updateMember(PutMemberDTO putMemberDTO) {
		Member newMemberInfo = memberConvert.putDTOToEntity(putMemberDTO);
		Member oldMemberInfo = this.getMember(newMemberInfo.getMemberId());

		// 抽出這次更新時的國籍
		 NationalityEnum oldMemberCountry = CountryUtil.getDomesticOrInternational(oldMemberInfo.getCountry());
		 NationalityEnum newMemberCountry = CountryUtil.getDomesticOrInternational(newMemberInfo.getCountry());

		// 如果國籍有變更 國外=>台灣 or 台灣=>國外 則拒絕變更
		if (!oldMemberCountry.equals(newMemberCountry)) {
			throw new MemberException("Nationality cannot be changed between domestic and foreign statuses.");
		}

		String oldMemberIdCard = oldMemberInfo.getIdCard();
		String newMemberIdCard = newMemberInfo.getIdCard();

		System.out.println("舊ID_Card: " + oldMemberIdCard);
		System.out.println("新ID_Card: " + newMemberIdCard);

		// 台灣人註冊
		if(NationalityEnum.DOMESTIC.equals(newMemberCountry)){
			// idCard有傳值，且跟舊資料不一致，idCard不許修改,因為這代表帳號
			if (newMemberIdCard != null && !oldMemberIdCard.equals(newMemberIdCard)) {
				throw new MemberException("身分證不允許更新，如需更新請洽工作人員");
			}
		}

		if (newMemberInfo.getIdCard() != null) {
			// 判斷要更新的身分證，是否已被註冊
			LambdaQueryWrapper<Member> idCardQueryWrapper = new LambdaQueryWrapper<>();
			idCardQueryWrapper.eq(Member::getIdCard, newMemberInfo.getIdCard());
			Long idCardCount = baseMapper.selectCount(idCardQueryWrapper);

			if (idCardCount > 0) {
				throw new RegisteredAlreadyExistsException(
						messageHelper.get(I18nMessageKey.Registration.Auth.ID_CARD_REGISTERED));
			}
		}

		// 如果前述條件都通過,進行更新
		baseMapper.updateById(newMemberInfo);

	}

	@Override
	public void updateMemberForAdmin(PutMemberForAdminDTO putMemberForAdminDTO) {
		Member member = memberConvert.putForAdminDTOToEntity(putMemberForAdminDTO);
		baseMapper.updateById(member);
	}

	@Override
	public void deleteMember(Long memberId) {
		baseMapper.deleteById(memberId);
	}

	@Override
	public void deleteMemberList(List<Long> memberIds) {
		baseMapper.deleteBatchIds(memberIds);
	}

	/** 以下跟登入有關 */

	@Override
	public SaTokenInfo login(Member member) {
		// 之後應該要以這個會員ID 產生Token 回傳前端，讓他直接進入登入狀態
		StpKit.MEMBER.login(member.getMemberId());

		// 登入後才能取得session
		SaSession session = StpKit.MEMBER.getSession();
		// 並對此token 設置會員的緩存資料
		session.set(MEMBER_CACHE_INFO_KEY, member);

		SaTokenInfo tokenInfo = StpKit.MEMBER.getTokenInfo();
		return tokenInfo;
	}

	/**
	 * 透過Member資訊,返回TokenInfo
	 * 
	 * @param member
	 * @return
	 */
	private SaTokenInfo returnSaTokenInfo(Member member) {
		// 之後應該要以這個會員ID 產生Token 回傳前端，讓他直接進入登入狀態
		StpKit.MEMBER.login(member.getMemberId());
		// 登入後才能取得session
		SaSession session = StpKit.MEMBER.getSession();
		// 並對此token 設置會員的緩存資料
		session.set(MEMBER_CACHE_INFO_KEY, member);
		return StpKit.MEMBER.getTokenInfo();
	}

	@Override
	public SaTokenInfo login(MemberEmailLogin memberLoginInfo) {
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getEmail, memberLoginInfo.getEmail())
				.eq(Member::getPassword, memberLoginInfo.getPassword());

		Member member = baseMapper.selectOne(memberQueryWrapper);

		if (member != null) {
			return this.returnSaTokenInfo(member);
		}

		// 如果 member為null , 則直接拋出異常
		throw new AccountPasswordWrongException(messageHelper.get(I18nMessageKey.Registration.Auth.WRONG_ACCOUNT));

	}

	@Override
	public SaTokenInfo login(MemberIdCardLogin memberIdCardLogin) {
		// 透過idCard 和 password 查詢Member資訊
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getIdCard, memberIdCardLogin.getIdCard())
				.eq(Member::getPassword, memberIdCardLogin.getPassword());

		Member member = baseMapper.selectOne(memberQueryWrapper);

		if (member != null) {
			return this.returnSaTokenInfo(member);
		}

		// 如果 member為null , 則直接拋出異常
		throw new AccountPasswordWrongException(messageHelper.get(I18nMessageKey.Registration.Auth.WRONG_ACCOUNT));

	}

	@Override
	public SaTokenInfo foreignLogin(MemberLoginDTO memberLoginDTO) {

		// 獲得本國國籍 <Taiwan>
		String national = CountryUtil.getHomeCountry();

		// 除了帳號和密碼，額外判斷國家不屬於 台灣
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getEmail, memberLoginDTO.getAccount())
				.eq(Member::getPassword, memberLoginDTO.getPassword())
				.ne(Member::getCountry, national);

		Member member = baseMapper.selectOne(memberQueryWrapper);

		if (member != null) {
			return this.returnSaTokenInfo(member);
		}

		// 如果 member為null , 則直接拋出異常
		throw new AccountPasswordWrongException(messageHelper.get(I18nMessageKey.Registration.Auth.WRONG_ACCOUNT));

	}

	@Override
	public SaTokenInfo localLogin(MemberLoginDTO memberLoginDTO) {
		// 獲得本國國籍 <Taiwan>
		String national = CountryUtil.getHomeCountry();

		// 除了帳號和密碼，額外判斷國家屬於 台灣
		LambdaQueryWrapper<Member> memberQueryWrapper = new LambdaQueryWrapper<>();
		memberQueryWrapper.eq(Member::getIdCard, memberLoginDTO.getAccount())
				.eq(Member::getPassword, memberLoginDTO.getPassword())
				.eq(Member::getCountry, national);

		Member member = baseMapper.selectOne(memberQueryWrapper);

		if (member != null) {
			return this.returnSaTokenInfo(member);
		}

		// 如果 member為null , 則直接拋出異常
		throw new AccountPasswordWrongException(messageHelper.get(I18nMessageKey.Registration.Auth.WRONG_ACCOUNT));

	}

	@Override
	public void logout() {
		// 根據token 直接做登出
		StpKit.MEMBER.logout();

	}

	@Override
	public Member getMemberInfo() {
		// 會員登入後才能取得session
		SaSession session = StpKit.MEMBER.getSession();
		// 獲取當前使用者的資料
		Member memberInfo = (Member) session.get(MEMBER_CACHE_INFO_KEY);
		return memberInfo;
	}

	/** ----------------------------以下跟Tag有關---------------------------------- */
	@Override
	public MemberTagVO getMemberTagVOByMember(Long memberId) {
		// 獲取member 資料並轉換成 memberTagVO
		Member member = baseMapper.selectById(memberId);
		return memberConvert.entityToMemberTagVO(member);
	}

	@Override
	public IPage<Member> getMemberPageByQuery(Page<Member> page, String queryText, Collection<Long> memberIds) {
		// 1.基於條件查詢 memberList
		LambdaQueryWrapper<Member> memberWrapper = new LambdaQueryWrapper<>();

		// 2.當 queryText 不為空字串、空格字串、Null 時才加入篩選條件
		memberWrapper
				.and(StringUtils.isNotBlank(queryText),
						wrapper -> wrapper.like(Member::getFirstName, queryText)
								.or()
								.like(Member::getLastName, queryText)
								.or()
								.like(Member::getChineseName, queryText)
								.or()
								.like(Member::getEmail, queryText)
								.or()
								.like(Member::getPhone, queryText)
								.or()
								.like(Member::getRemitAccountLast5, queryText))
				.in(!memberIds.isEmpty(), Member::getMemberId, memberIds);

		// 3.查詢 MemberPage (分頁)
		return baseMapper.selectPage(page, memberWrapper);
	}

	@Override
	public Map<Long, Member> getMemberMap() {
		// 1.高效獲取所有會員
		List<Member> members = this.getMembersEfficiently();
		// 2.返回key為 memberId, value為Member 的Map 對象
		return members.stream().collect(Collectors.toMap(Member::getMemberId, Function.identity()));
	}

	@Override
	public Map<Long, Member> getMemberMapByIds(Collection<Long> memberIds) {
		// 1.用memberId列表查詢Member資料
		List<Member> memberList = this.getMemberListByIds(memberIds);
		// 2.Member資料轉為memberId為key , Member本身為值的Map對象
		return memberList.stream().collect(Collectors.toMap(Member::getMemberId, Function.identity()));
	}

	@Override
	public Map<Long, Member> getMemberMapByAttendeeList(Collection<Attendee> attendeeList) {
		// 1.提取attendee的memberId,拿到memberId列表
		Set<Long> memberIdSet = attendeeList.stream().map(Attendee::getMemberId).collect(Collectors.toSet());
		// 2.用memberId列表查詢Member資料
		List<Member> memberList = this.getMemberListByIds(memberIdSet);
		// 3.Member資料轉為memberId為key , Member本身為值的Map對象
		return memberList.stream().collect(Collectors.toMap(Member::getMemberId, Function.identity()));
	}

}
