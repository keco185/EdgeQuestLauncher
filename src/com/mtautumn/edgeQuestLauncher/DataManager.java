package com.mtautumn.edgeQuestLauncher;

import javax.swing.JFrame;

public class DataManager {
	public JFrame window;
	public String[] messages = new String[0];
	public ComponentMover cm;
	public WindowMouseListener wml;
	public boolean launchVisible;
	public boolean downloadVisible;
	public int downloadingState = 0;
	public boolean running = true;
	public boolean windowReady = false;
	public String downloadText = "";
	public String launchText = "";
	public String humanGameVersion = "";
	public String humanOnlineVersion = "";
}
