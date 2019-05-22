package it.telecomitalia.TIMgamepad2;

public class CalibrationGamepadVO {
	protected String GamepadName;
	protected String MACAddress;
	protected boolean online;
	
	public CalibrationGamepadVO(String name, String mac, boolean on) {
		this.GamepadName = name;
		this.MACAddress = mac;
		this.online = on;
	}
	
	public void setGamepadName(String name) {
		this.GamepadName = name;
	}
	public void setMACAddress(String mac) {
		this.MACAddress = mac;
	}
	
	public String getGamepadName() {
		return GamepadName;
	}
	
	public String getMACAddress() {
		return MACAddress;
	}
}
