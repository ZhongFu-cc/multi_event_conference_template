package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.addEntityDTO.AddArticleDTO;
import tw.com.conference.pojo.DTO.putEntityDTO.PutArticleDTO;
import tw.com.conference.pojo.entity.Article;

@Mapper(componentModel = "spring")
public interface ArticleConvert {

	Article addDTOToEntity(AddArticleDTO insertArticleDTO);

	Article putDTOToEntity(PutArticleDTO updateArticleDTO);
	
	Article copyEntity(Article article);
	
}
