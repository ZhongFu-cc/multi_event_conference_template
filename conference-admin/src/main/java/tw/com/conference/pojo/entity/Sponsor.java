package tw.com.conference.pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tw.com.conference.enums.SponsorTierEnum;

/**
 * <p>
 * 贊助商
 * </p>
 */
@ToString
@Getter
@Setter
@TableName("sponsor")
@Schema(name = "Sponsor", description = "贊助商")
public class Sponsor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Schema(description = "主鍵ID")
	@TableId("sponsor_id")
	private Long sponsorId;

	@Schema(description = "贊助商名稱")
	@TableField("name")
	private String name;

	@Schema(description = "贊助商 Logo URL")
	@TableField("logo_url")
	private String logoUrl;

	@Schema(description = "贊助等級 diamond:鑽石級, platinum:白金級, gold:金級, silver:銀級, bronze:銅級, none:無級別")
	@TableField("tier")
	private SponsorTierEnum tier;

	@Schema(description = "Logo 縮放比例 1-100, 預設100")
	@TableField("scale")
	private Integer scale;

	@Schema(description = "排序 (數字越小越前面)")
	@TableField("sort")
	private Integer sort;

	@Schema(description = "創建者")
	@TableField(value = "create_by", fill = FieldFill.INSERT)
	private String createBy;

	@Schema(description = "創建時間")
	@TableField(value = "create_date", fill = FieldFill.INSERT)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createDate;

	@Schema(description = "最後修改者")
	@TableField(value = "update_by", fill = FieldFill.UPDATE)
	private String updateBy;

	@Schema(description = "最後修改時間")
	@TableField(value = "update_date", fill = FieldFill.UPDATE)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateDate;

	@Schema(description = "邏輯刪除,預設為0活耀,1為刪除")
	@TableField("is_deleted")
	@TableLogic
	private Integer isDeleted;
}
