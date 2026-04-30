package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddFormResponseDTO;
import tw.com.conference.pojo.VO.FormResponseVO;
import tw.com.conference.pojo.entity.FormResponse;

@Mapper(componentModel = "spring")
public interface FormResponseConvert {

    // 宣告默認映射 , 告訴 MapStruct 如何把 CommonStatusEnum → Integer
//    default Integer commonStatusEnumMapToInteger(CommonStatusEnum status) {
//        return status == null ? null : status.getValue();
//    }
	
	FormResponse addDTOToEntity(AddFormResponseDTO formResponseDTO);
	
	FormResponseVO entityToVO(FormResponse formResponse);
	
}
