package tw.com.conference.convert;

import org.mapstruct.Mapper;

import tw.com.conference.pojo.DTO.PutPaperReviewDTO;
import tw.com.conference.pojo.VO.AssignedReviewersVO;
import tw.com.conference.pojo.VO.ReviewerScoreStatsVO;
import tw.com.conference.pojo.entity.PaperAndPaperReviewer;

@Mapper(componentModel = "spring")
public interface PaperAndPaperReviewerConvert {


	PaperAndPaperReviewer putDTOToEntity(PutPaperReviewDTO putPaperReviewDTO);

	AssignedReviewersVO entityToAssignedReviewersVO(PaperAndPaperReviewer paperAndPaperReviewer);

	ReviewerScoreStatsVO entityToReviewerScoreStatsVO(PaperAndPaperReviewer paperAndPaperReviewer);
}
