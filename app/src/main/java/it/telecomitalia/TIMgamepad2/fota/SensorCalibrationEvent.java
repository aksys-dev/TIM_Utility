package it.telecomitalia.TIMgamepad2.fota;

public interface SensorCalibrationEvent {
	void getGyroscopeValue(int x, int y, int z);
	void getSavedGyroZero(int x, int y, int z);
	void progressCalibration(int progress, int max);
	void startCalibration();
	void endCalibration();
}
