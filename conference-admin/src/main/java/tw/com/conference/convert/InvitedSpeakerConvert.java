package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddInvitedSpeakerDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutInvitedSpeakerDTO;
import tw.com.conference.pojo.entity.InvitedSpeaker;

@Mapper(componentModel = "spring")
public interface InvitedSpeakerConvert {

	InvitedSpeaker addDTOToEntity(AddInvitedSpeakerDTO addInvitedSpeakerDTO);

	InvitedSpeaker putDTOToEntity(PutInvitedSpeakerDTO putInvitedSpeakerDTO);
	
	
}
