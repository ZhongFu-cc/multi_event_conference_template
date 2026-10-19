package tw.com.conference.pojo.DTO.putEntityDTO;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.CommonStatusEnum;

/**
 * 修改優惠碼活動<br>
 * <br>
 * 刻意不開放修改 codeType / discountType / discountValue：
 * 碼一旦發出去，其折扣語意不該再變動，否則已持碼的人拿到的優惠會與當初宣告的不同。
 * 要改折扣內容請停用此 campaign 另開一個。
 */
@Data
public class PutPromoCampaignDTO {

	@NotNull
	@Schema(description = "主鍵ID")
	private Long promoCampaignId;

	@NotBlank
	@Schema(description = "優惠碼活動名稱")
	private String name;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "可使用起始日, 不填為不限")
	private LocalDate dateFrom;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@Schema(description = "可使用結束日, 不填為不限")
	private LocalDate dateTo;

	@NotNull
	@Schema(description = "是否啟用;0=否,1=是")
	private CommonStatusEnum isActive;

}
