// IGamePadService.aidl
package it.telecomitalia.TIMgamepad2;

// Declare any non-default types here with import statements

interface IGamePadService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    int setGamePadMode(byte mode);

    String getVersion(int device);

    void setVibrationState(int id, int status);
}
