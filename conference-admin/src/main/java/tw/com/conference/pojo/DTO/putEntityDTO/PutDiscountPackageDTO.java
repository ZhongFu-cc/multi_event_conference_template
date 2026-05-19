package tw.com.conference.pojo.DTO.putEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PutDiscountPackageDTO {

	@NotNull
    @Schema(description = "主鍵ID")
    private Long discountPackageId;

	@NotBlank
    @Schema(description = "活動組合優惠名稱")
    private String name;
	
}
