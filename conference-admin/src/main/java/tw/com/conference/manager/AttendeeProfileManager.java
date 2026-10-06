package tw.com.conference.manager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.zxing.WriterException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tw.com.conference.convert.AttendeeConvert;
import tw.com.conference.handler.AttendeeVOHandler;
import tw.com.conference.helper.TagAssignmentHelper;
import tw.com.conference.pojo.BO.CheckinInfoBO;
import tw.com.conference.pojo.BO.PresenceStatsBO;
import tw.com.conference.pojo.DTO.EmailBodyContent;
import tw.com.conference.pojo.DTO.WalkInRegistrationDTO;
import tw.com.conference.pojo.VO.AttendeeStatsVO;
import tw.com.conference.pojo.VO.AttendeeVO;
import tw.com.conference.pojo.VO.CheckinRecordVO;
import tw.com.conference.pojo.VO.ImportResultVO;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.AttendeeEvent;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.excelPojo.AttendeeExcel;
import tw.com.conference.pojo.excelPojo.AttendeeUpdateExcel;
import tw.com.conference.service.AsyncService;
import tw.com.conference.exception.CheckinRecordException;
import tw.com.conference.service.AttendeeEventService;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.CheckinRecordService;
import tw.com.conference.service.EventService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.MemberTagService;
import tw.com.conference.service.NotificationService;
import tw.com.conference.service.TagService;
import tw.com.conference.utils.QrcodeUtil;

/**
 * AttendeeProfileManager，處理與會者個人資料相關的管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendeeProfileManager {

	@Value("${project.name}")
	private String PROJECT_NAME;

	@Value("${project.banner-url}")
	private String BANNER_PHOTO_URL;

	private final TagAssignmentHelper tagAssignmentHelper;
	private final MemberService memberService;
	private final MemberTagService memberTagService;
	private final AttendeeService attendeeService;
	private final AttendeeEventService attendeeEventService;
	private final AttendeeConvert attendeeConvert;
	private final EventService eventService;
	private final RegistrationEventManager registrationEventManager;
	private final CheckinRecordService checkinRecordService;
	private final TagService tagService;
	private final NotificationService notificationService;
	private final AsyncService asyncService;

	private final AttendeeVOHandler attendeeVOHandler;

	/**
	 * 根據 attendeeId 獲取 與會者完整資訊
	 * 
	 * @param attendeeId
	 * @return
	 */
	public AttendeeVO getAttendeesVO(Long attendeeId) {
		return attendeeVOHandler.getAttendeeVO(attendeeId);
	}

	/**
	 * 返回所有attendeeVO對象
	 * 
	 * @return
	 */
	public List<AttendeeVO> getAttendeesVOList() {
		// 1.獲取所有與會者資料
		List<Attendee> attendeeList = attendeeService.getAttendeeList();

		// 2.轉換並返回VOList
		return attendeeVOHandler.getAttendeeVOsByAttendeeList(attendeeList);

	}

	/**
	 * 返回所有attendeeVO 分頁對象
	 * 
	 * @param page
	 * @return
	 */
	public IPage<AttendeeVO> getAttendeesVOPage(Page<Attendee> page) {
		// 1.獲取與會者分頁對象
		IPage<Attendee> attendeePage = attendeeService.getAttendeePage(page);
		// 2.轉換並返回VOList
		List<AttendeeVO> attendeeVOList = attendeeVOHandler.getAttendeeVOsByAttendeeList(attendeePage.getRecords());
		// 3.封裝成VOpage
		Page<AttendeeVO> attendeeVOPage = new Page<>(attendeePage.getCurrent(), attendeePage.getSize(),
				attendeePage.getTotal());
		attendeeVOPage.setRecords(attendeeVOList);

		return attendeeVOPage;
	}

	/**
	 * 返回當前與會者簽/退的統計資料
	 * 
	 * @return
	 */
	public AttendeeStatsVO getAttendeeStatsVO(Long eventId) {

		// eventId 為 null 時維持整體統計 (與會者層級)，有帶時改為該場次的統計
		boolean byEvent = eventId != null;

		AttendeeStatsVO attendeeStatsVO = new AttendeeStatsVO();

		//1.查詢 應到 人數
		// 場次：該場已繳費的報名數；整體：所有與會者
		Integer countTotalShouldAttend = byEvent ? (int) attendeeEventService.countPaidByEventId(eventId)
				: attendeeService.countTotalShouldAttend();
		attendeeStatsVO.setTotalShouldAttend(countTotalShouldAttend);

		//2.查詢 已簽到 人數
		Integer countCheckedIn = byEvent ? checkinRecordService.getCountCheckedInByEventId(eventId)
				: checkinRecordService.getCountCheckedIn();
		attendeeStatsVO.setTotalCheckedIn(countCheckedIn);
		//未簽到人數
		attendeeStatsVO.setTotalNotArrived(countTotalShouldAttend - countCheckedIn);

		//3.查詢 尚在現場、已離場 人數
		PresenceStatsBO presenceStatsBO = byEvent ? checkinRecordService.getPresenceStatsByEventId(eventId)
				: checkinRecordService.getPresenceStats();
		attendeeStatsVO.setTotalOnSite(presenceStatsBO.getTotalOnsite());
		attendeeStatsVO.setTotalLeft(presenceStatsBO.getTotalLeft());

		return attendeeStatsVO;
	}

	/**
	 * 現場註冊報名
	 * 
	 * @param walkInRegistrationDTO
	 * @return
	 */
	@Transactional
	public CheckinRecordVO walkInRegistration(WalkInRegistrationDTO walkInRegistrationDTO) {
		// 1.創建Member對象，新增進member table
		Member member = memberService.addMemberOnSite(walkInRegistrationDTO);

		// 2.獲取當下Member群體的Index,進行會員標籤分組
		tagAssignmentHelper.assignTag(member.getMemberId(), memberService::getMemberGroupIndex,
				tagService::getOrCreateMemberGroupTag, memberTagService::addMemberTag);

		// 3.現場報到未指定活動時，預設報名主活動
		List<Long> eventIds = walkInRegistrationDTO.getEventIds();
		if (eventIds == null || eventIds.isEmpty()) {
			eventIds = List.of(eventService.getMain().getEventId());
		}

		// 4.免費報名 (預設他會在現場繳費完成)：產生 0 元已付款訂單、報名紀錄，並成為與會者
		registrationEventManager.freeRegistration(member, eventIds);

		// 5.免費報名完成後即為與會者，取回與會者資料
		Attendee attendee = attendeeService.getAttendeeByMemberId(member.getMemberId());
		if (attendee == null) {
			throw new CheckinRecordException("現場報到失敗，未能建立與會者資料");
		}

		// 6.獲取AttendeesVO
		AttendeeVO attendeeVO = this.getAttendeesVO(attendee.getAttendeeId());

		// 7.現場報到當下簽到的是「第一個指定的場次」(未指定時即為主活動)
		Long checkinEventId = eventIds.get(0);
		AttendeeEvent attendeeEvent = attendeeEventService.getByAttendeeAndEvent(attendee.getAttendeeId(),
				checkinEventId);
		if (attendeeEvent == null) {
			throw new CheckinRecordException("現場報到失敗，未能建立該場活動的報名紀錄");
		}

		// 8.產生簽到記錄並組裝返回VO
		CheckinRecordVO checkinRecordVO = checkinRecordService.walkInRegistration(attendee.getAttendeeId(),
				attendeeEvent.getAttendeeEventId());
		checkinRecordVO.setAttendeeVO(attendeeVO);

		// 9.產生現場註冊的信件,包含QRcode信息
		EmailBodyContent walkInRegistrationContent = notificationService
				.generateWalkInRegistrationContent(attendee.getAttendeeId(), BANNER_PHOTO_URL);

		// 10.透過異步工作去寄送郵件，因為使用了事務，在事務提交後才執行寄信的異步操作，安全做法
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				asyncService.sendCommonEmail(member.getEmail(), "【" + PROJECT_NAME + " 報到確認】現場報到用 QR Code 及活動資訊",
						walkInRegistrationContent.getHtmlContent(), walkInRegistrationContent.getPlainTextContent());
			}
		});

		// 11.返回簽到顯示格式
		return checkinRecordVO;
	}

	/**
	 * 刪除與會者 及 其簽到/退紀錄
	 * 
	 * @param attendeeId
	 */
	public void deleteAttendees(Long attendeeId) {
		// 1.刪除與會者的簽到/退紀錄
		checkinRecordService.deleteCheckinRecordByAttendeeId(attendeeId);

		// 2.刪除與會者
		attendeeService.deleteAttendee(attendeeId);

	}

	/**
	 * 批量刪除與會者 及 其簽到/退紀錄
	 * 
	 * @param attendeeIds
	 */
	public void batchDeleteAttendees(List<Long> attendeeIds) {
		for (Long attendeeId : attendeeIds) {
			this.deleteAttendees(attendeeId);
		}

	}

	/**
	 * 下載與會者Excel
	 * 
	 * @param response
	 * @throws IOException
	 */
	public void downloadExcel(HttpServletResponse response) throws IOException {

		// 1.基礎設定
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		// 这里URLEncoder.encode可以防止中文乱码 ， 和easyexcel没有关系
		String fileName = URLEncoder.encode("與會者名單", "UTF-8").replaceAll("\\+", "%20");
		response.setHeader("Content-disposition", "attachment;filename*=" + fileName + ".xlsx");

		// 2.獲取所有會員的映射對象
		Map<Long, Member> memberMap = memberService.getMemberMap();

		// 3.高效獲取所有attendee
		List<Attendee> attendeeList = attendeeService.getAttendeesEfficiently();

		// 4.資料轉換成Excel
		List<AttendeeExcel> excelData = attendeeList.stream().map(attendee -> {

			// 4-1放入Member轉換成VO對象
			AttendeeVO attendeeVO = attendeeConvert.entityToVO(attendee);
			attendeeVO.setMember(memberMap.get(attendee.getMemberId()));
			AttendeeExcel attendeeExcel = attendeeConvert.voToExcel(attendeeVO);

			// 4-2 獲取與會者的簡易簽到記錄
			CheckinInfoBO checkinInfoBO = checkinRecordService
					.getLastCheckinRecordByAttendeeId(attendee.getAttendeeId());
			attendeeExcel.setFirstCheckinTime(checkinInfoBO.getCheckinTime());
			attendeeExcel.setLastCheckoutTime(checkinInfoBO.getCheckoutTime());

			// 4-3匯出專屬簽到/退 QRcode
			try {
				attendeeExcel.setQRcodeImage(
						QrcodeUtil.generateBase64QRCode(attendeeVO.getAttendeeId().toString(), 200, 200));
			} catch (WriterException | IOException e) {
				log.error("QRcode產生失敗");
				e.printStackTrace();
			}

			return attendeeExcel;

		}).collect(Collectors.toList());

		EasyExcel.write(response.getOutputStream(), AttendeeExcel.class).sheet("與會者列表").doWrite(excelData);

	}

	/**
	 * 匯入Excel 批量更新與會者 收據統編號碼
	 * 
	 * @param file
	 * @throws IOException
	 */
	public ImportResultVO importExcelUpdate(MultipartFile file) throws IOException {

		// 統一返回結果的對象
		ImportResultVO result = new ImportResultVO();

		/**
		 * Excel 搭配監聽器讀取,避免一次讀取避免OOM
		 * 
		 * @param 檔案的inputStream
		 * @param 對應的Class
		 * @param 監聽器內部方法
		 * 
		 */
		EasyExcel.read(file.getInputStream(), AttendeeExcel.class, new ReadListener<AttendeeExcel>() {

			// 批次數量
			private static final int BATCH_COUNT = 500;
			// 更新暫存列表
			private List<Attendee> cachedDataList = new ArrayList<>();

			// 每讀取到一行就執行invoke函數
			@Override
			public void invoke(AttendeeExcel row, AnalysisContext context) {

				// Excel 行號從1開始
				int rowIndex = context.readRowHolder().getRowIndex() + 1;
				result.setTotalCount(result.getTotalCount() + 1);

				// 初始化attendeeId用來記錄,如果從excel中成功讀取就會有值
				String attendeeId = "unknown";
				if (row != null && row.getAttendeeId() != null) {
					attendeeId = row.getAttendeeId().toString();
				}

				try {

					//轉換資料
					AttendeeUpdateExcel excelToUpdatePojo = attendeeConvert.excelToUpdatePojo(row);
					Attendee attendee = attendeeConvert.updatePojoToEntity(excelToUpdatePojo);
					cachedDataList.add(attendee);

					if (cachedDataList.size() >= BATCH_COUNT) {
						attendeeService.saveOrUpdateBatch(cachedDataList);
						result.setSuccessCount(result.getSuccessCount() + cachedDataList.size());
						cachedDataList.clear();
					}
				} catch (Exception e) {
					// 捕獲單行錯誤，不影響整個批次
					String messageWithId = String.format("主鍵ID=%s, %s", attendeeId, e.getMessage());
					result.getFailList().add(new ImportResultVO.FailDetail(rowIndex, messageWithId));

					log.error("第 {} 行資料處理失敗: {}", rowIndex, messageWithId, e);
				}

			}

			// 當 Excel 全部讀完後，會呼叫 doAfterAllAnalysed()。
			@Override
			public void doAfterAllAnalysed(AnalysisContext context) {
				// 如果緩存內還有檔案,則最後再更新一次
				if (!cachedDataList.isEmpty()) {
					try {
						attendeeService.saveOrUpdateBatch(cachedDataList);
						result.setSuccessCount(result.getSuccessCount() + cachedDataList.size());
					} catch (Exception e) {
						// 批次失敗直接記錄所有行為失敗
						int rowStart = result.getTotalCount() - cachedDataList.size() + 1;
						for (int i = 0; i < cachedDataList.size(); i++) {
							Attendee attendee = cachedDataList.get(i);
							int rowNumber = rowStart + i;
							String attendeeIdBatch = attendee.getAttendeeId() != null
									? attendee.getAttendeeId().toString()
									: "unknown";
							String messageBatch = String.format("主鍵ID=%s, %s", attendeeIdBatch, e.getMessage());

							result.getFailList().add(new ImportResultVO.FailDetail(rowNumber, messageBatch));

							log.error("第 {} 行批次更新失敗: {}", rowNumber, messageBatch, e);
						}
					}

				}
				// 從錯誤清單中拿到總數
				result.setFailCount(result.getFailList().size());
			}
		}).sheet().doRead();

		return result;

	}

}
