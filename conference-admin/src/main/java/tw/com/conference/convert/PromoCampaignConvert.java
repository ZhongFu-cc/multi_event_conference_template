package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddPromoCampaignDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPromoCampaignDTO;
import tw.com.conference.pojo.entity.PromoCampaign;

@Mapper(componentModel = "spring")
public interface PromoCampaignConvert {

	PromoCampaign addDTOToEntity(AddPromoCampaignDTO addPromoCampaignDTO);

	PromoCampaign putDTOToEntity(PutPromoCampaignDTO putPromoCampaignDTO);

}
