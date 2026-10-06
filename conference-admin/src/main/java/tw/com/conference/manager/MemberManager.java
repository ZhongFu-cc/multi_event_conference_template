package tw.com.conference.manager;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import tw.com.conference.exception.CheckinRecordException;
import tw.com.conference.exception.MemberException;
import tw.com.conference.pojo.entity.Attendee;
import tw.com.conference.pojo.entity.Member;
import tw.com.conference.pojo.entity.Orders;
import tw.com.conference.pojo.entity.Setting;
import tw.com.conference.service.AttendeeService;
import tw.com.conference.service.CheckinRecordService;
import tw.com.conference.service.MemberService;
import tw.com.conference.service.OrdersService;
import tw.com.conference.service.SettingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberManager {

	@Value("${project.email.reply-to}")
	private String EMAIL_REPLY_TO;

	@Value("${project.rate}")
	private Long RATE;

	// 參加證明 Template Path
	private final String CERTIFICATE_TEMPLATE_PATH = "jasperTemplate/certificate.jasper";
	private final String CERTIFICATE_TEMPLATE_BG_PATH = "jasperTemplate/certificate.jpg";

	// Invoice Template Path
	private final String INVOICE_TEMPLATE_PATH = "jasperTemplate/conference_invoice.jasper";
	private final String INVOICE_SUBREPORT_TEMPLATE_PATH = "jasperTemplate/orderItems.jasper";
	private final String INVOICE_TEMPLATE_BG_PATH = "jasperTemplate/conference_invoice.jpg";

	private final MemberService memberService;
	private final OrdersService ordersService;
	private final AttendeeService attendeeService;
	private final CheckinRecordService checkinRecordService;
	private final SettingService settingService;

	/**
	 * 產生參加證明
	 * 
	 * @param response
	 * @param memberId
	 * @throws IOException
	 */
	public void generateCertificate(HttpServletResponse response, Long memberId) throws IOException {

		// 1.查詢會員是否是與會者的資格
		Attendee attendee = attendeeService.getAttendeeByMemberId(memberId);
		if (attendee == null) {
			throw new CheckinRecordException("會員未繳費並非與會者");
		}

		// 2.查詢與會者是否有簽到記錄，如果不是用我們的簽到系統,或者不需要那麼嚴格就註解掉
		long checkinRecordCount = checkinRecordService.getCheckinRecordCountByAttendeeId(attendee.getAttendeeId());
		if (checkinRecordCount < 1) {
			throw new CheckinRecordException("與會者沒有簽到記錄，不發參加證明");
		}

		// 3.引入certificate(參加證明)  Jasper文件(模板)
		Resource resource = new ClassPathResource(CERTIFICATE_TEMPLATE_PATH);
		InputStream mainInputStream = resource.getInputStream();
		// 4.引入certificate(參加證明) 背景圖片
		Resource bgResource = new ClassPathResource(CERTIFICATE_TEMPLATE_BG_PATH);
		InputStream bgInputStream = bgResource.getInputStream();

		// 5.透過response得到響應輸出流
		ServletOutputStream outputStream = response.getOutputStream();

		// 6.準備資料,製作參加證明 PDF
		try {

			// 6-1 初始化要給報表的Paramter Map對象
			Map<String, Object> parameters = new HashMap<>();

			// 6-2 拿到Member的資料
			Member member = memberService.getMember(memberId);

			// 6-3 準備證書上的姓名
			String firstName = StringUtils.trimToEmpty(member.getFirstName());
			String lastName = StringUtils.trimToEmpty(member.getLastName());

			// 只有在 first / last 非空時才加入，避免出現多餘空格
			String enName = Stream.of(firstName, lastName)
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(" "));
			String chinese = StringUtils.trimToEmpty(member.getChineseName());

			// 最後組合,先中文,後英文
			String finalName = Stream.of(chinese, enName)
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(" "));

			// 6-4 放入模板需要的資料
			parameters.put("finalName", finalName);
			parameters.put("bg", bgInputStream);

			/**
			 * 填充報表
			 * 
			 * 務必!!要以三個參數來創建,儘管第三個參數數據源為空,不填寫編譯時也不會報錯,但最終PDF數據都會為空
			 * 第一個參數為: 文件輸入流 InputStream , 準確來說是 主報表 .jasper文件
			 * 第二個參數為: Map對象 向模板中輸入的參數 $P{} ,
			 * 通常是String、InputStream、List、Set這類的,SubReport常搭配List、Set使用
			 * 第三個參數為: JasperDataSource 數據源(和Mysql數據源不同,這代表的是要填入的數據) , $F{}
			 * 第三個參數可以是Connection , 可以是Java Bean , 可以是Map,沒有時也務必new
			 * JREmptyDataSource()來替代
			 * 
			 */
			JasperPrint print = JasperFillManager.fillReport(mainInputStream, parameters, new JREmptyDataSource());

			// 3.將JasperPrint以PDF形式輸出
			// 透過JasperExportManager工具類使用exportReportToPdfFile
			// 傳遞第一個參數JasperPrint對象
			// 傳遞第二個參數outputStream
			JasperExportManager.exportReportToPdfStream(print, outputStream);

		} catch (JRException e) {
			log.error(e.getMessage());
			e.printStackTrace();

		} finally {
			// 最終關閉這個響應輸出流 , 以及輸入流
			outputStream.close();
			mainInputStream.close();
			bgInputStream.close();
		}

	}

	/**
	 * 產生繳費證明
	 * 
	 * @param response
	 * @param memberId
	 * @throws IOException
	 */
	public void generateConferenceInvoice(HttpServletResponse response, Long memberId) throws IOException {

		// 1.該會員所有已付款的訂單，全部列入繳費證明；沒有任何已付款訂單則不給予
		List<Orders> paidOrders = ordersService.findPaidOrdersByMemberId(memberId);
		if (paidOrders.isEmpty()) {
			throw new MemberException("會員未繳任何活動費用 , 不給予Invoice");
		}

		// 2.引入 Invoice(繳費證明) Jasper文件(模板) + 子報表模板
		Resource resource = new ClassPathResource(INVOICE_TEMPLATE_PATH);
		Resource subReportResource = new ClassPathResource(INVOICE_SUBREPORT_TEMPLATE_PATH);
		InputStream mainInputStream = resource.getInputStream();
		InputStream subReportInputStream = subReportResource.getInputStream();

		// 3.引入 Invoice(繳費證明) 背景圖片
		Resource bgResource = new ClassPathResource(INVOICE_TEMPLATE_BG_PATH);
		InputStream bgInputStream = bgResource.getInputStream();

		// 4.透過response得到響應輸出流,不做設置直接響應
		ServletOutputStream outputStream = response.getOutputStream();

		// 5.準備資料,製作參加證明 PDF
		try {
			// 5-1 初始化要給報表的Paramter Map對象
			Map<String, Object> parameters = new HashMap<>();

			// 5-2拿到活動日期
			Setting setting = settingService.getSetting();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d ,yyyy", Locale.ENGLISH);
			String eventDate = setting.getEventStartDate().format(formatter);

			// 5-3業務上來說,有繳費一定是與會者,這邊就不多判斷
			Member member = memberService.getMember(memberId);

			// 5-4準備姓名,去掉空格
			String firstName = StringUtils.trimToEmpty(member.getFirstName());
			String lastName = StringUtils.trimToEmpty(member.getLastName());
			String enName = Stream.of(firstName, lastName)
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining(" "));

			// 5-5與會者資料
			Attendee attendee = attendeeService.getAttendeeByMemberId(memberId);

			// 5-6 每張已付款訂單的台幣金額，以美金折算匯率換算，保留兩位小數，四捨五入
			BigDecimal rate = new BigDecimal(RATE);
			BigDecimal usdTotal = BigDecimal.ZERO;
			for (Orders order : paidOrders) {
				BigDecimal usdAmount = order.getTotalAmount().divide(rate, 2, RoundingMode.HALF_UP);
				// 報表直接顯示美元金額
				order.setTotalAmount(usdAmount);
				usdTotal = usdTotal.add(usdAmount);
			}

			parameters.put("finalName", enName);
			parameters.put("eventDate", eventDate);
			parameters.put("sequenceNo", String.format("%03d", attendee.getSequenceNo()));
			parameters.put("totalAmount", usdTotal);
			parameters.put("contactEmail", EMAIL_REPLY_TO);
			parameters.put("bg", bgInputStream);
			parameters.put("subReport", subReportInputStream);

			parameters.put("orderItems", paidOrders);

			/**
			 * 填充報表
			 * 
			 * 務必!!要以三個參數來創建,儘管第三個參數數據源為空,不填寫編譯時也不會報錯,但最終PDF數據都會為空
			 * 第一個參數為: 文件輸入流 InputStream , 準確來說是 主報表 .jasper文件
			 * 第二個參數為: Map對象 向模板中輸入的參數 $P{},
			 * 通常是String、InputStream、List、Set這類的,SubReport常搭配List、Set使用
			 * 第三個參數為: JasperDataSource 數據源(和Mysql數據源不同,這代表的是要填入的數據) , $F{}
			 * 第三個參數可以是Connection , 可以是Java Bean , 可以是Map,沒有時也務必new
			 * JREmptyDataSource()來替代
			 * 
			 */
			JasperPrint print = JasperFillManager.fillReport(mainInputStream, parameters, new JREmptyDataSource());

			// 3.將JasperPrint以PDF形式輸出
			// 透過JasperExportManager工具類使用exportReportToPdfFile
			// 傳遞第一個參數JasperPrint對象
			// 傳遞第二個參數outputStream
			JasperExportManager.exportReportToPdfStream(print, outputStream);
		} catch (JRException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			// 最終關閉這個響應輸出流
			outputStream.close();
			mainInputStream.close();
			subReportInputStream.close();
			bgInputStream.close();
		}
	}

	/**
	 * 刪除單個會員<br>
	 * 包含其與會者身分 和 簽到退紀錄
	 * 
	 * @param memberId
	 */
	@Transactional
	public void deleteMember(Long memberId) {
		// 1.刪除會員的與會者身分
		Attendee attendee = attendeeService.deleteAttendeeByMemberId(memberId);

		// 2.如果attendee不為null，刪除他的簽到/退紀錄
		if (attendee != null) {
			checkinRecordService.deleteCheckinRecordByAttendeeId(attendee.getAttendeeId());
		}

		// 3.最後刪除自身
		memberService.deleteMember(memberId);

	}

	/**
	 * 批量刪除單個會員<br>
	 * 包含其與會者身分 和 簽到退紀錄
	 * 
	 * @param memberId
	 */
	@Transactional
	public void deleteMemberList(Collection<Long> memberIds) {
		for (Long memberId : memberIds) {
			this.deleteMember(memberId);
		}
	}

}
