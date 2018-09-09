package it.telecomitalia.TIMgamepad2.model;

/**
 * 公共类
 * Created by czy on 2017/5/11.
 */

public class Constant {

    public static final String SPNAME =

            "IPMSSP";
    public static final String ISENTERINTO = "ISENTERINTO";
    public static final String LASTCHECKTIME = "LASTCHECKTIME";

    public static final String ReleaseUrl = "http://ota1.gamepadota.com:18081/api";//韩国服务器

    public static final String TestUrl = "http://192.168.1.252:10006/api"; //内部测试服务器

    public static final String ReleaseUrl1 = "http://ota.gamepadota.com:18081/api";//意大利服务器


    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    /**
     * gamapd1的json地址
     */
    public static final String REMOTE_CONFIG_URL_GAMEPAD1_NEW = "http://192.168.1.252:10006/api/device/15/firmware/latest/";

    public static final String REMOTE_CONFIG_URL_KOREA_NEW = "http://ota1.gamepadota.com:28081/api/device/6/firmware/latest/";

    /**
     * 下载固件成功上传的API
     */
    public static final String DOWNLOAD_SUCCESS_URL = ReleaseUrl + "/firmware/%d/download/success/";
    /**
     * 更新固件成功上传的API
     */
    public static final String UPDATE_SUCCESS_URL = ReleaseUrl + "/firmware/%d/update/success/";
    /**
     * 更新固件失败上传的API
     */
    public static final String UPDATE_FAIL_URL = ReleaseUrl + "/firmware/%d/update/fail/";

    public static final int DOWNLOAD_SUCCESS = 0;

    public static final int UPDATE_SUCCESS = 1;

    public static final int UPDATE_FAIL = 2;


    public static int size = 16 * 1024;//文件的实际大小

    /**
     * 固件保存名称
     */
    public static final String GAMEPADE = "remote.dat";
    public static final String NEWGAMEPADE = "newremote.bin";

    /**
     * gampad1从服务器获取的json信息保存到本地的位置
     */
    public static final String CONFIG_FILE_NAME_GAMEPAD1 = "upgrade_configuration.xml";

    //把状态信息发送到ContentProiver地址
    public static final String URI_STATE = "content://timvision.telemetry/traps";

    /**
     * eventName
     */
    public static final String GAMEPAD_UPGRADE_EVENT_APP = "SV_OPEN";
    public static final String GAMEPAD_UPGRADE_EVENT_GAMEPAD1 = "UPDATE_GAMEPAD1";
    public static final String GAMEPAD_UPGRADE_EVENT_GAMEPAD2 = "UPDATE_GAMEPAD2";
    public static final String GAMEPAD_UPGRADE_EVENT_DONGLE = "UPDATE_DONGLE";
    public static final String GAMEPAD_UPGRADE_EVENT_NAME = "GAMEPAD_UPGRADE";

    /**
     * device
     */

    public static final String GAMEPAD_UPGRADE_EVENT_DEVICE_GAMPAD1 = "gamepad1";
    public static final String GAMEPAD_UPGRADE_EVENT_DEVICE_GAMPAD2 = "gamepad2";
    public static final String GAMEPAD_UPGRADE_EVENT_DEVICE_DONGLE = "dongle ";


    /**
     * eventType
     */
    public static final String GAMEPAD_UPGRADE_EVENT_TYPE_USER_INPUT = "userInput";
    public static final String GAMEPAD_UPGRADE_EVENT_TYPE_SYS_INPUT = "systemInput";
    public static final String GAMEPAD_UPGRADE_EVENT_TYPE_BUFFER = "buffering";


    /**
     * 事件状态
     */
    public static final String GAMEPAD_UPGRADE_STATUS_OPEN = "open";
    public static final String GAMEPAD_UPDATE_SUCCESS = "upadate_success";
    public static final String GAMEPAD_UPDATE_FAIL = "upadate_fail";


    public static final String TIMEOUTERROR = "Impossibile recuperare la data dell ultima verifica. Controllare la connessione Internet.";
}
