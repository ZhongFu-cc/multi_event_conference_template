package tw.com.conference.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tw.com.conference.enums.CommonStatusEnum;
import tw.com.conference.validation.annotation.ValidRequireLoginIfMultipleSubmissions;
import tw.com.conference.validation.constraint.HasLoginAndMultipleSubmissionRules;

public class RequireLoginIfMultipleSubmissionsValidator
		implements ConstraintValidator<ValidRequireLoginIfMultipleSubmissions, HasLoginAndMultipleSubmissionRules> { // Use AddFormDTO or a common interface if both DTOs share it

	@Override
	public boolean isValid(HasLoginAndMultipleSubmissionRules dto, ConstraintValidatorContext context) {
		if (dto == null) {
			return true; // Null DTOs are valid by default; handle elsewhere if needed
		}

		CommonStatusEnum allowMultiple = dto.getAllowMultipleSubmissions();
		CommonStatusEnum requireLogin = dto.getRequireLogin();

		// If allowMultipleSubmissions is true (ONE), requireLogin must also be true (ONE)
		if (allowMultiple == CommonStatusEnum.YES && requireLogin != CommonStatusEnum.YES) {
			return false;
		}

		return true;
	}
}