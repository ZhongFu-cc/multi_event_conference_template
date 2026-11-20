package tw.com.conference.pojo.VO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tw.com.conference.enums.SponsorTierEnum;

/**
 * 依贊助等級分組的贊助商，給前端顯示用
 */
@Data
public class SponsorTierGroupVO {

	@Schema(description = "贊助等級 diamond:鑽石級, platinum:白金級, gold:金級, silver:銀級, bronze:銅級, none:無級別")
	private SponsorTierEnum tier;

	@Schema(description = "贊助等級說明")
	private String tierLabel;

	@Schema(description = "此等級下的贊助商，依 sort 排序")
	private List<SponsorVO> sponsorList;

}
