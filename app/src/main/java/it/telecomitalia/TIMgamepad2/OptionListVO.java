package it.telecomitalia.TIMgamepad2;

public class OptionListVO {
	String mainText;
	String subText;
	public OptionListVO(String main, String sub) {
		mainText = main;
		subText = sub;
	}
	public String getMainText() { return mainText; }
	public String getSubText() { return subText; }
}
