package it.telecomitalia.TIMgamepad2.Proxy;

public enum CommonEventCode {
    REQ_CHECK_ACTIVATION(0x01),
    REQ_ENABLE_TIMESTAMP(0x02),
    REQ_DISABLE_TIMESTAMP(0x03),
    REQ_QUERY_TIMESTAMP_STATUS(0x04),

    RES_ACTIVATED(0x05),
    RES_SERVER_ERROR(0x06),
    RES_TIMESTAMP_ENABLED(0x07),
    RES_TIMESTAMP_DISABLED(0x08),

    REQ_IMU_DATA(0x09),

    CODE_OK(0x00),
    CODE_ERROR(0x7F);

    private final int value;


    CommonEventCode(int value) {
        this.value = value;
    }

    public static CommonEventCode valueOf(int value) {
        switch (value) {
            case 0x01:
                return CommonEventCode.REQ_CHECK_ACTIVATION;
            case 0x02:
                return CommonEventCode.REQ_ENABLE_TIMESTAMP;
            case 0x03:
                return CommonEventCode.REQ_DISABLE_TIMESTAMP;
            case 0x04:
                return CommonEventCode.REQ_QUERY_TIMESTAMP_STATUS;
            case 0x05:
                return CommonEventCode.RES_ACTIVATED;
            case 0x06:
                return CommonEventCode.RES_SERVER_ERROR;
            case 0x07:
                return CommonEventCode.RES_TIMESTAMP_ENABLED;
            case 0x08:
                return CommonEventCode.RES_TIMESTAMP_DISABLED;
            case 0x09:
                return CommonEventCode.REQ_IMU_DATA;
            case 0x00:
                return CommonEventCode.CODE_OK;
            case 0xFF:
                return CommonEventCode.CODE_ERROR;
            default:
                return null;
        }
    }

    public static byte value(CommonEventCode code) {
        switch (code) {
            case CODE_OK:
                return 0x00;
            case CODE_ERROR:
                return 0x0F;
            case REQ_CHECK_ACTIVATION:
                return 0x01;
            case REQ_ENABLE_TIMESTAMP:
                return 0x02;
            case REQ_DISABLE_TIMESTAMP:
                return 0x03;
            case REQ_QUERY_TIMESTAMP_STATUS:
                return 0x04;
            case RES_ACTIVATED:
                return 0x05;
            case RES_SERVER_ERROR:
                return 0x06;
            case RES_TIMESTAMP_ENABLED:
                return 0x07;
            case REQ_IMU_DATA:
                return 0x09;
            case RES_TIMESTAMP_DISABLED:
                return 0x7F;
            default:
                return 0;

        }
    }
}
