package it.telecomitalia.TIMgamepad2;

interface IGamePadService {

    int setGamePadMode(byte mode);

    // get Gamepad Firmware Version
    String getVersion(int device);

    // Send Vibration Code
    void setVibrationState(int id, int left_status, int right_status);
}
