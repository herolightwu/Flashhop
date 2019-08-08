package com.flashhop.app.utils;

import android.os.Environment;

import com.flashhop.app.R;

public class Const {

    public final static String APP_TAG = "Flashhop";
    public final static String WHAT_IS_LINK = "https://www.16personalities.com/free-personality-test";

    public static String APP_PHOTOS_PATH = "/Flashhop/photos";
    public static final String PHOTO_DIR = Environment.getExternalStorageDirectory().getPath() + APP_PHOTOS_PATH ;
    public static final int cover_res[] = new int[]{0, R.drawable.category_party, R.drawable.category_eating, R.drawable.category_dating, R.drawable.category_sports,
            R.drawable.category_outdoor, R.drawable.category_game, R.drawable.category_study, R.drawable.category_spiritual, R.drawable.category_arts};
    public static String interest_list[] = new String[]{"Party", "Eating", "Dating", "Sports", "Outdoors", "Games", "Study", "Spiritual", "Arts"};
    public static final String tag_list[] = new String[]{"Salty", "Foodie", "Nutty", "Jock", "Flakey", "Reliable", "Adventurous", "Outgoing", "Witty",
            "Gamer", "Geek", "Leader", "Quiet", "Low key", "Basic"};

    public static int VIEW_FILTER_ALL = 0;
    public static int VIEW_FILTER_PEOPLE = 1;
    public static int VIEW_FILTER_EVENT = 2;

    public static int HOME_HOME = 0;
    public static int HOME_MINE = 1;
    public static int HOME_CHAT = 2;
    public static int HOME_ALARM = 3;
    public static int HOME_EVENT = 4;
    public static int HOME_MSG = 5;
    public static int HOME_PREVIEW = 6;
    public static int HOME_PROFILE = 7;
    public static int HOME_EDIT_PROFILE = 8;

    public final static String FRAG_HOME_TAG = "FRAG_HOME";
    public final static String FRAG_MINE_TAG = "FRAG_MINE";
    public final static String FRAG_CHAT_TAG = "FRAG_CHAT";
    public final static String FRAG_ALARM_TAG = "FRAG_ALARM";
    public final static String FRAG_PROFILE_TAG = "FRAG_PROFILE";
    public final static String FRAG_EVENT_TAG = "FRAG_HOST_EVENT";
    public final static String FRAG_PUBLISH_EVENT_TAG = "FRAG_PUBLISH_EVENT";
    public final static String FRAG_EVENT_LIST_TAG = "FRAG_EVENT_LIST";
    public final static String FRAG_UPCOMING_TAG = "FRAG_UPCOMING";

    public final static String FRAG_CHAT_GROUP = "FRAG_CHAT_GROUP";
    public final static String FRAG_GROUP_INFO = "FRAG_GROUP_INFO";
    public final static String FRAG_HOPPERS_TAG = "FRAG_HOPPERS";
    public final static String FRAG_TIPS_TAG = "FRAG_TIPS";

    public final static int CHAT_GROUP_F = 0;
    public final static int CHAT_HANGOUT_F = 1;
    public final static int ALARM_FRIEND_F = 2;
    public final static int ALARM_ME_F = 3;

    public final static String HOST_URL = "https://flashhop.com/";//http://35.183.220.114/";
    public final static String SIGNUP_URL = "api/register";
    public final static String LOGIN_URL = "api/login";
    public final static String SOCIAL_URL = "api/registerSocialLogin";
    public final static String FORGOT_URL = "api/password/request_reset";
    public final static String RESET_URL = "api/password/resetpassword";
    public final static String VERIFY_URL = "api/emailVerify";
    public final static String RESEND_URL = "api/sendVerifyCode";
    public final static String LOGOUT_URL = "api/logout";
    public final static String REG_PROFILE_URL = "api/registerUserProfile";
    public final static String CHECK_TOKEN = "api/chkToken";
    public final static String PERSONALITY_TYPES_URL = "api/personality_types";//get
    public final static String UPLOAD_PROFILE_URL = "api/updateUserProfile";
    public final static String EMAIL_RESET_URL = "api/emailResetRequest";
    public final static String CHANGE_EMAIL_URL = "api/changeEmail";
    public final static String CHANGE_PASSWORD_URL = "api/changePassword";
    public final static String CHANGE_INTERESTS_URL = "api/changeInterests";
    public final static String CHANGE_LANGS_URL = "api/changeLang";
    public final static String NOTIFICATION_SETTING_URL = "api/changeNotificationSetting";
    public final static String CHANGE_PRIVACY_URL = "api/changePrivacy";
    public final static String INACTIVE_URL = "api/changeActive";
    public final static String PUBLISH_EVENT_URL = "api/hostEvent";
    public final static String FILTER_URL = "api/filter";
    public final static String EDIT_EVENT_URL = "api/editEvent";
    public final static String JOIN_EVENT_URL = "api/joinEvent";
    public final static String LEAVE_EVENT_URL = "api/leaveEvent";
    public final static String INVITE_FRIENDS_URL = "api/inviteFriends";
    public final static String LIKE_URL = "api/likeDisLike";
    public final static String CANCEL_EVENT = "api/cancelEvent";
    public final static String TAGS_URL = "api/tags";
    public final static String INSERT_TAG_URL = "api/insert_tag";
    public final static String LOCATION_UPDATE_API = "api/pinUserLocation";
    public final static String GPS_UPDATE_API = "api/updateUserLocation";
    public final static String EVENT_LIKE_DISLIKE = "api/eventLikeDislike";
    public final static String ADD_MY_FRIEND = "api/addToMyFriend";
    public final static String REMOVE_MY_FRIEND = "api/removeFromMyFriend";
    public final static String REPORT_URL = "api/report";
    public final static String ALL_FRIENDS_URL = "api/getAllFriends";
    public final static String UPCOMING_EVENT_URL = "api/getUpcomingEvents";
    public final static String MY_HOSTING_EVENT_URL = "api/getUpcomingHostedEvents";
    public final static String WHATS_UP_FRIENDS_URL = "api/getWhatsUpFriends";
    public final static String WHATS_UP_ME_URL = "api/getWhatsUpForMe";
    public final static String SUPER_LIKE_URL = "api/responseSuperLike";
    public final static String SUPER_DISS_URL = "api/responseSuperDiss";
    public final static String ACCEPT_REJECT_FRIENDS_URL = "api/acceptRejectFriendRequest";
    public final static String HANGOUTS_URL = "api/hangouts";
    public final static String EVENT_CHAT_MUTE = "api/chatMuteEvent";
    public final static String SEND_CHAT_PUSH = "api/sendChatPush";
    public final static String PAY_WITH_STRIPE = "api/payWithStripeAPI";
    public final static String READ_EVENT_URL = "api/readEvent";
    public final static String UPDATE_PAID_STATE_URL = "api/updatePaidStatus";
    public final static String TIP_LIST_URL = "api/getTipList";
    public final static String UPLOAD_FILE_URL = "api/uploadFile";
    public final static String UPDATE_DEBIT_ACCOUNT = "api/updateCustomAccount";
    public final static String GET_DEBIT_CARD = "api/getDebitCardData";
    public final static String GET_PAYMENT_TRANSACTION = "api/getTransferList";

    public final static String PLACE_DETAIL_API = "https://maps.googleapis.com/maps/api/place/details/json?placeid=%s&fields=name,formatted_address,geometry&key=%s";
    public static String autocomplete_api = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&location=%f,%f&radius=50000&strictbounds&key=%s";//&types=establishment
    public static String autocomplete_api_origin = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&key=%s";

    public final static int ALARM_ITEM_TYPE_MSG = 0;
    public final static int ALARM_ITEM_TYPE_EVENT = 1;

    public final static int MSG_TYPE_TEXT = 0;
    public final static int MSG_TYPE_PHOTO = 1;
    public final static int MSG_TYPE_VOICE = 2;
    public final static int MSG_TYPE_DOC = 3;

}
