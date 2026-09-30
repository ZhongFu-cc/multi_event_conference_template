package tw.com.conference.pojo.VO;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.OrderStatusEnum;
import tw.com.conference.pojo.BO.AppliedDiscountBO;

/**
 * 後台審核用：報名某活動但尚未繳費的會員，及其包含此活動的訂單
 */
@Data
public class EventUnpaidMemberVO {

	@Schema(description = "會員ID")
	private Long memberId;

	@Schema(description = "中文姓名")
	private String chineseName;

	@Schema(description = "名字")
	private String firstName;

	@Schema(description = "姓氏")
	private String lastName;

	@Schema(description = "E-Mail")
	private String email;

	@Schema(description = "電話")
	private String phone;

	@Schema(description = "國家")
	private String country;

	@Schema(description = "匯款帳號-後五碼")
	private String remitAccountLast5;

	@Schema(description = "包含此活動的訂單ID; 若會員尚無包含此活動的未付訂單則為 null")
	private Long ordersId;

	@Schema(description = "訂單狀態: 未付款 / 付款-待確認")
	private OrderStatusEnum orderStatus;

	@Schema(description = "訂單原價總額")
	private BigDecimal originalTotalAmount;

	@Schema(description = "訂單折扣總額")
	private BigDecimal totalDiscountAmount;

	@Schema(description = "訂單應付總額")
	private BigDecimal totalAmount;

	@Schema(description = "訂單套用的折扣明細")
	private List<AppliedDiscountBO> appliedDiscounts;

	@Schema(description = "此訂單包含的所有活動標題 (通過此訂單會一併通過這些活動)")
	private List<String> orderEventTitles;

}
