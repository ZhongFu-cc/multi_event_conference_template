package tw.com.conference.pojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignEvent2PackageDTO {

	@Schema(description = "活動事件  ID 列表")
	@NotNull(message = "活動事件 ID 列表不能為null")
	private List<Long> eventIds;

	@Schema(description = "優惠組合 ID")
	@NotNull(message = "優惠組合 ID 不能為null")
	private Long discountPackageId;

}
