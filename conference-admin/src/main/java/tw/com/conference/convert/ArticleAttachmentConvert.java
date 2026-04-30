package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddArticleAttachmentDTO;
import tw.com.conference.pojo.entity.ArticleAttachment;

@Mapper(componentModel = "spring")
public interface ArticleAttachmentConvert {
	ArticleAttachment addDTOToEntity(AddArticleAttachmentDTO addArticleAttachmentDTO);
}
