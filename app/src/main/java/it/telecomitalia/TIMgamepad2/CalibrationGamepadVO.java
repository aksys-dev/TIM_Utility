package it.telecomitalia.TIMgamepad2;

public class CalibrationGamepadVO {
	protected String GamepadName;
	protected String MACAddress;
	
	public CalibrationGamepadVO(String name, String mac) {
		this.GamepadName = name;
		this.MACAddress = mac;
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
