package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddGroupDiscountRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutGroupDiscountRuleDTO;
import tw.com.conference.pojo.entity.GroupDiscountRule;

@Mapper(componentModel = "spring")
public interface GroupDiscountRuleConvert {

	GroupDiscountRule addDTOToEntity(AddGroupDiscountRuleDTO addGroupDiscountRuleDTO);

	GroupDiscountRule putDTOToEntity(PutGroupDiscountRuleDTO putGroupDiscountRuleDTO);

}
