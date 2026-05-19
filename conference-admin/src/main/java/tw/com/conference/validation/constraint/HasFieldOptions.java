package tw.com.conference.validation.constraint;

import tw.com.conference.enums.FormFieldTypeEnum;
import tw.com.conference.pojo.DTO.FormFieldOptionDTO;

public interface HasFieldOptions {

	public FormFieldTypeEnum getFieldType();
	
	public FormFieldOptionDTO getOptions();
	
}
