package tw.com.conference.pojo.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SponsorVO {

	@Schema(description = "主鍵ID")
	private Long sponsorId;

	@Schema(description = "贊助商名稱")
	private String name;

	@Schema(description = "贊助商 Logo URL")
	private String logoUrl;

	@Schema(description = "Logo 縮放比例 1-100")
	private Integer scale;

	@Schema(description = "排序 (數字越小越前面)")
	private Integer sort;

}
