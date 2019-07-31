package com.mtautumn.edgeQuestLauncher;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

public class WindowMouseListener implements MouseListener {
	public boolean launchGame = false;
	public boolean downloadGame = false;
	public DataManager dataManager;
	public WindowMouseListener(JFrame window, DataManager dataManager) {
		window.addMouseListener(this);
		this.dataManager = dataManager;
	}
	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (x > 500 && x < 750) {
			if (y > 200 && y < 275) {
				downloadGame = true;
			} else if (y > 325 && y < 400) {
				launchGame = true;
			}
		} else if (x <= 40 && x >= 0 && y <= 40 && y >= 0) {
			dataManager.running = false;
			System.exit(0);
		}
	}
}
