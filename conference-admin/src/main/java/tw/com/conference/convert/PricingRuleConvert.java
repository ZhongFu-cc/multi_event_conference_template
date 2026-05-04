package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddPricingRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutPricingRuleDTO;
import tw.com.conference.pojo.entity.PricingRule;

@Mapper(componentModel = "spring")
public interface PricingRuleConvert {

	PricingRule addDTOToEntity(AddPricingRuleDTO addPricingRuleDTO);

	PricingRule putDTOToEntity(PutPricingRuleDTO putPricingRuleDTO);

}
