package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddMemberTypeDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutMemberTypeDTO;
import tw.com.conference.pojo.entity.MemberType;

@Mapper(componentModel = "spring")
public interface MemberTypeConvert {

	MemberType addDTOToEntity(AddMemberTypeDTO addMemberTypeDTO);

	MemberType putDTOToEntity(PutMemberTypeDTO putMemberTypeDTO);
	

}
