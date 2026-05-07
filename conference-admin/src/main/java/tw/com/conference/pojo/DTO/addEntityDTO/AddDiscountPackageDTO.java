package tw.com.conference.pojo.DTO.addEntityDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddDiscountPackageDTO {

	@NotBlank
    @Schema(description = "活動組合優惠名稱")
    private String name;
}
