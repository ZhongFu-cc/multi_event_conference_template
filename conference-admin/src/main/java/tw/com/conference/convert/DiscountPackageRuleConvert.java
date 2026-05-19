package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddDiscountPackageRuleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutDiscountPackageRuleDTO;
import tw.com.conference.pojo.entity.DiscountPackageRule;

@Mapper(componentModel = "spring")
public interface DiscountPackageRuleConvert {

	DiscountPackageRule addDTOToEntity(AddDiscountPackageRuleDTO addDiscountPackageRuleDTO);

	DiscountPackageRule putDTOToEntity(PutDiscountPackageRuleDTO putDiscountPackageRuleDTO);
	

}
