package it.telecomitalia.TIMgamepad2.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CommerHelper {
    public static final String SPNAME = "IPMSSP";
    public static final String ISENTERINTO = "ISENTERINTO";
    public static final String LASTCHECKTIME = "LASTCHECKTIME";
    public static final String IS_COMMIT = "IS_COMMIT";

    public static String inttohex(int value) {
        return Integer.toHexString(value);
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        //int length = hexString.length() / 2;
        int length = 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static String toStringHex(String s) {
        if ("0x".equals(s.substring(0, 2))) {
            s = s.substring(2);
        }
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                        i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            s = new String(baKeyword, "utf-8");//UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


    public static File getFile(byte[] data, String filePath, String fileName) {

        FileOutputStream fos = null;
        File file = null;

        File dir = new File(filePath);
        if (!dir.exists() && dir.isDirectory()) {
            dir.mkdirs();
        }
        file = new File(filePath + fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            fos = new FileOutputStream(file);

            fos.write(data, 0, data.length);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;

    }

    public static byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];

        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }

    public static String gettime() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(new Date());
    }

    public static String HexToString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = "0x0" + hex.toUpperCase() + " ";
            } else {
                hex = "0x" + hex.toUpperCase() + " ";
            }
            result += hex;
        }
        return result;
    }

    public static String HexToString(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = "0x0" + hex.toUpperCase() + " ";
        } else {
            hex = "0x" + hex.toUpperCase() + " ";
        }
        return hex;
    }
}
