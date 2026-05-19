package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.pojo.DTO.addEntityDTO.AddFormDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutFormDTO;
import tw.com.conference.pojo.VO.FormVO;
import tw.com.conference.pojo.entity.Form;

@Mapper(componentModel = "spring")
public interface FormConvert {

    // 宣告默認映射 , 告訴 MapStruct 如何把 CommonStatusEnum → Integer
    default Integer commonStatusEnumMapToInteger(CommonStatusEnum status) {
        return status == null ? null : status.getValue();
    }
	
	Form addDTOToEntity(AddFormDTO addFormDTO);
	
	Form putDTOToEntity(PutFormDTO putFormDTO);
	
	FormVO entityToVO(Form form);
	
}
