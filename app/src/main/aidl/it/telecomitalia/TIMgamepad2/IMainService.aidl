// IMainService.aidl
package it.telecomitalia.TIMgamepad2;

// Declare any non-default types here with import statements

interface IMainService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    int setGamePadModeInternal(byte mode);

    String getVersionInternal(int device);

    void setVibrationStateInternal(int id, int status);

}
