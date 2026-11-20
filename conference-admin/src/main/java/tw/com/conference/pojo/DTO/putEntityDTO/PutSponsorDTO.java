package tw.com.conference.pojo.DTO.putEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tw.com.conference.enums.SponsorTierEnum;

@Data
public class PutSponsorDTO {

	@NotNull
	@Schema(description = "主鍵ID")
	private Long sponsorId;

	@NotBlank
	@Schema(description = "贊助商名稱")
	private String name;

	@NotNull
	@Schema(description = "贊助等級 diamond:鑽石級, platinum:白金級, gold:金級, silver:銀級, bronze:銅級, none:無級別")
	private SponsorTierEnum tier;

	@NotNull
	@Min(1)
	@Max(100)
	@Schema(description = "Logo 縮放比例 1-100")
	private Integer scale;

	@Schema(description = "排序 (數字越小越前面)")
	private Integer sort;

}
