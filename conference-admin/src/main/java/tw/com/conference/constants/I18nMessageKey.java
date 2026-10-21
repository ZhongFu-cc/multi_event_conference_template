package tw.com.conference.constants;

import lombok.Getter;
import lombok.Setter;


/**
 * I18N 信息key <br>
 * 會與 src/main/resources 裡的 messages_*.properties 匹配
 * 
 */
@Getter
@Setter
public class I18nMessageKey {
	// 通用錯誤key
	public static final String DEFAULT = "default";

	// 註冊相關key
	public static final class Registration {
		public static final String CLOSED = "registration.closed";
		public static final String EVENT_ALREADY_REGISTERED = "registration.event-already-registered";
		
		// 團體註冊
		public static final class Group {
			public static final String CLOSED = "registration.group.closed";
			public static final String NOT_ALLOWED = "registration.group.not-allowed";
			public static final String NO_RULE = "registration.group.no-rule";
			public static final String NOT_FOUND = "registration.group.not-found";
			public static final String INVITE_CODE_INVALID = "registration.group.invite-code-invalid";
			public static final String NOT_RECRUITING = "registration.group.not-recruiting";
			public static final String NOT_LOCKED = "registration.group.not-locked";
			public static final String ALREADY_PAID = "registration.group.already-paid";
			public static final String ALREADY_JOINED = "registration.group.already-joined";
			public static final String IN_OTHER_GROUP = "registration.group.in-other-group";
			public static final String LEADER_ONLY = "registration.group.leader-only";
			public static final String NOT_MEMBER = "registration.group.not-member";
			public static final String MEMBER_NOT_FOUND = "registration.group.member-not-found";
			public static final String LEADER_CANNOT_LEAVE = "registration.group.leader-cannot-leave";
			public static final String BELOW_MIN_COUNT = "registration.group.below-min-count";
			public static final String MEMBER_ALREADY_REGISTERED = "registration.group.member-already-registered";
			public static final String STATUS_CHANGED = "registration.group.status-changed";
		}
		
	    // 帳號驗證相關
	    public static final class Auth {
	        public static final String EMAIL_NOT_FOUND = "registration.auth.email-not-found";
	        public static final String ACCOUNT_REGISTERED = "registration.auth.account-registered";
	        public static final String EMAIL_REGISTERED = "registration.auth.email-registered";
	        public static final String ID_CARD_REGISTERED = "registration.auth.id_card-registered";
	        public static final String WRONG_ACCOUNT = "registration.auth.wrong-account";

	    }

	}

	// 付款相關key
	public static final class Payment {
		
		public static final String CLOSED = "payment.closed";
		public static final String ORDER_NOT_FOUND = "payment.order-not-found";

		// 團體付款
		public static final class Group {
			public static final String CLOSED = "payment.group.closed";
			public static final String MUST_BE_PRIMARY = "payment.group.must-be-primary";
		}

	}

	// 優惠碼相關的key
	public static final class PromoCode {
		public static final String NOT_FOUND = "promo-code.not-found";
		public static final String INACTIVE = "promo-code.inactive";
		public static final String NOT_STARTED = "promo-code.not-started";
		public static final String EXPIRED = "promo-code.expired";
		public static final String EXHAUSTED = "promo-code.exhausted";
		public static final String NOT_APPLICABLE = "promo-code.not-applicable";
	}

	// 投稿相關的key
	public static final class Paper {
		public static final String CLOSED = "paper.closed";
		public static final String NO_MATCH = "paper.no-match";
		public static final String PREPAID = "paper.prepaid";

		// 投稿附件相關的key
		public static final class Attachment {
			public static final String NO_MATCH = "paper.attachment.no-match";
			public static final String FILE_SIZE = "paper.attachment.file-size";
			public static final String FILE_TYPE = "paper.attachment.file-type";
		}
	}
}
