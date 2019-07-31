package com.mtautumn.edgeQuestLauncher;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

class Window extends JComponent {
	private static final long serialVersionUID = 1L;
	String[] messages;
	boolean select;
	boolean download;
	boolean launch;
	int downloadingState;
	String downloadText;
	String launchText;
	public Window(String[] messages, boolean select, boolean download, boolean launch, int downloading, String downloadText, String launchText) {
		this.messages = messages;
		this.select = select;
		this.download = download;
		this.launch = launch;
		this.downloadingState = downloading;
		this.downloadText = downloadText;
		this.launchText = launchText;
	}
	public void paint (Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.DARK_GRAY);
		g2.fillRect(0, 0, 800, 600);
		BufferedImage img = null;
		BufferedImage cover = null;
		BufferedImage downloading = null;
		BufferedImage downloaded = null;
		BufferedImage close = null;
		try {
			if (select) {
				img = ImageIO.read(Launcher.class.getResourceAsStream("bothSelect.png"));
			} else {
				img = ImageIO.read(Launcher.class.getResourceAsStream("both.png"));
			}
			cover = ImageIO.read(Launcher.class.getResourceAsStream("cover.png"));
			close = ImageIO.read(Launcher.class.getResourceAsStream("close.png"));
			downloading = ImageIO.read(Launcher.class.getResourceAsStream("downloading.png"));
			downloaded = ImageIO.read(Launcher.class.getResourceAsStream("downloaded.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		g2.drawImage(img, 0, 0, 800, 600, null);
		if (!download) {
			g2.drawImage(cover, 500, 200, 250, 75, null);
		}
		if (!launch) {
			g2.drawImage(cover, 500, 325, 250, 75, null);
		}
		if (downloadingState == 1) {
			g2.drawImage(downloading, 500, 500, 250, 75, null);
		} else if (downloadingState == 2) {
			g2.drawImage(downloaded, 500, 500, 250, 75, null);
		}
		g2.setColor(Color.WHITE);
		for (int i = 0; i < messages.length; i++) {
			//g2.drawString(messages[i], 12, 200 - 30 * (i+1));
		}
		g2.drawImage(close, 0, 0, 40, 40, null);
		FontMetrics fm = g2.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(downloadText, g2);
        int x = (250 - (int) r.getWidth()) / 2 + 500;
        int y = 270;
		g2.drawString(downloadText, x, y);
		r = fm.getStringBounds(launchText, g2);
        x = (250 - (int) r.getWidth()) / 2 + 500;
        y = 395;
		g2.drawString(launchText, x, y);
	}
}
public class WindowManager extends Thread{
	public DataManager dataManager;
	public WindowManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}
	public void run() {
		setupWindow();
		while(dataManager.running) {
			try {
				if (shouldUpdate()) {
					updateWindow();
				}
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		dataManager.window.setVisible(false);
		dataManager.window.dispose();
	}
	private void setupWindow() {
		dataManager.window = new JFrame();
		dataManager.window.setUndecorated(true);
		dataManager.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			dataManager.window.setIconImage(Toolkit.getDefaultToolkit().getImage(Launcher.class.getResource("logo.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataManager.window.setTitle("Edgequest");
		dataManager.window.setName("Edgequest");
		dataManager.window.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		dataManager.window.setBounds(screenSize.width / 2 - 400, screenSize.height / 2 - 300, 800, 600);
		dataManager.window.setResizable(false);
		dataManager.window.setBackground(Color.DARK_GRAY);
		dataManager.cm = new ComponentMover();
		dataManager.cm.registerComponent(dataManager.window);
		dataManager.wml = new WindowMouseListener(dataManager.window, dataManager);
		dataManager.windowReady = true;
	}
	private void updateWindow() {
		dataManager.window.getContentPane().removeAll();
		dataManager.window.getContentPane().add(new Window(dataManager.messages, dataManager.wml.downloadGame, dataManager.downloadVisible, dataManager.launchVisible, dataManager.downloadingState, dataManager.downloadText, dataManager.launchText));
		dataManager.window.setVisible(true);
	}
	String[] message;
	boolean downloadGame = false;
	boolean downloadVisible = false;
	boolean launchVisible = false;
	int downloadingState = 0;
	String downloadText = "";
	String launchText = "";
	private boolean shouldUpdate() {
		boolean out = false;
		if (message == null) {
			message = dataManager.messages;
			out = true;
		} else {
			if (!message.equals(dataManager.messages)) {
				out = true;
				message = dataManager.messages;
			}
		}

		if (downloadGame != dataManager.wml.downloadGame) {
			out = true;
			downloadGame = dataManager.wml.downloadGame;
		}
		
		if (downloadVisible != dataManager.downloadVisible) {
			out = true;
			downloadVisible = dataManager.downloadVisible;
		}
		
		if (launchVisible != dataManager.launchVisible) {
			out = true;
			launchVisible = dataManager.launchVisible;
		}
		if (downloadingState != dataManager.downloadingState) {
			out = true;
			downloadingState = dataManager.downloadingState;
		}
		
		if (!downloadText.equals(dataManager.downloadText)) {
			out = true;
			downloadText = dataManager.downloadText;
		}
		
		if (!launchText.equals(dataManager.launchText)) {
			out = true;
			launchText = dataManager.launchText;
		}
		return out;
	}
}
