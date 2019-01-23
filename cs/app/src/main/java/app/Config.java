package app;

public class Config {

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";

    public static final int DELAY = 10000;

    // The version of this application
    public static final String VERSION = "0.019";

    // The interval of which readings of a Sensing task are taken in minutes
    public static final int TASK_INTERVAL = 9;

    // Dsiplay online users on the map during task creation
    public static final boolean SHOW_USERS_ON_MAP = false;
}
