package tw.com.conference.manager;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tw.com.conference.service.MemberTypeService;

@Component
@RequiredArgsConstructor
public class CategoryPricingRuleManager {

	private final MemberTypeService memberTypeService;

	/**
	 * 移除會員身份類別
	 * @param eventId
	 */
	public void removeCategory(Long memberTypeId) {

		// 刪除事件會員身份類別 相關的 價格策略

		// 刪除事件會員身份類別 本身
		memberTypeService.remove(memberTypeId);
	}

}
