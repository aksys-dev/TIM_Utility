package it.telecomitalia.TIMgamepad2;

public class GamepadVO {
	protected final String unc = "Unknown";
	protected String GamepadName;
	protected String MACAddress;
	protected int Battery;
	protected String Firmware;
	boolean NeedUpdate;

	public GamepadVO(String mac, int batt, String firm) {
		GamepadName = unc;
		MACAddress = mac;
		Battery = batt;
		Firmware = firm;
		NeedUpdate = false;
	}

	public GamepadVO(String mac) {
		GamepadName = unc;
		MACAddress = mac;
		Battery = -1;
		Firmware = unc;
		NeedUpdate = false;
	}

	public GamepadVO() {
		GamepadName = unc;
		MACAddress = unc;
		Battery = -1;
		Firmware = unc;
		NeedUpdate = false;
	}

	public void setGamepadName(String name) { GamepadName = name; }
	public void setMACAddress(String mac) { MACAddress = mac; }
	public void setBattery(int value) { Battery = value; }
	public void setFirmware(String version) { Firmware = version; }

	public String getGamepadName() { return GamepadName; }
	public String getMACAddress() { return MACAddress ; }
	public int getBattery() { return Battery ; }
	public String getFirmware() { return Firmware ; }
}
