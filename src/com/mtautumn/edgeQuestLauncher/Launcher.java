package com.mtautumn.edgeQuestLauncher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Launcher {
	public static DataManager dataManager;
	private static String os;
	private static Path gameDir;
	private static String slash;
	private static int version = 0;
	private static final String OnlineURL = "https://keco.site/edgequest/";
	private static WindowManager windowManager;
	private static void log(String message) {
		System.out.println(message);
		String[] newMessages = new String[dataManager.messages.length + 1];
		newMessages[0] = message;
		for (int i = 0; i < dataManager.messages.length; i++) {
			newMessages[i+1] = dataManager.messages[i];
		}
		dataManager.messages = newMessages;
	}
	public static void main(String[] args) {
		dataManager = new DataManager();
		windowManager = new WindowManager(dataManager);
		windowManager.start();
		
		//Wait for window to be setup before continuing
		while (!dataManager.windowReady) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		log("Getting Operating System");
		setOS();
		log("Set game directory to " + gameDir.toString());
		
		
		//Setup game directory if it doesn't exist
		if (Files.notExists(gameDir)) {
			dataManager.downloadVisible = true;
			dataManager.launchVisible = false;
			dataManager.launchText = "Game not downloaded";
			dataManager.downloadText = "Checking server...";
			log("Creating game directory (Did not exist)");
			gameDir.toFile().mkdir();
		}
		
		
		//Get currently downloaded version of game
		File versionFile = new File(gameDir.toString() + slash + "version.txt");
		if(versionFile.exists() && !versionFile.isDirectory()) { 
			version = getVersion(versionFile.getAbsolutePath());
			if (version > 0) {
				dataManager.launchVisible = true;
				dataManager.launchText = "Version: " + dataManager.humanGameVersion;
			}
			log("Current game version number is " + version);
		} else {
			version = 0;
			log("Setting game version number to " + version);
			dataManager.downloadVisible = true;
			dataManager.launchText = "Game not downloaded";
		}
		
		
		//Get latest version available for download
		int onlineVersion = getOnlineVersion(OnlineURL + "/version.txt");
		if (onlineVersion > -1) {
			log("Current version available is " + onlineVersion);
		} else {
			log("Could not connect to game distribution server");
		}
		
		
		if (onlineVersion > version) {
			dataManager.downloadVisible = true;
			dataManager.downloadText = "Version: " + dataManager.humanOnlineVersion;
			while (!dataManager.wml.downloadGame && ((!dataManager.wml.launchGame && dataManager.launchVisible) || !dataManager.launchVisible)){
				if (!dataManager.launchVisible) {
					dataManager.wml.launchGame = false;
				}
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (dataManager.wml.downloadGame) {
				dataManager.downloadingState = 1;
				dataManager.downloadText = "Downloading version: " + dataManager.humanOnlineVersion;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dataManager.wml.downloadGame = false;
				log("Using online version");
				log("Downloading update");
				if (downloadLatest(OnlineURL + "/game.zip", gameDir + slash + "edgequest.zip")) {
					log("Update Downloaded");
					dataManager.downloadText = "Deleting old game files";
					log("Deleting old game");
					delete(new File(gameDir + slash + "game"));
					dataManager.downloadText = "Extracting game";
					log("Extracting update");
					extractGame(gameDir + slash + "edgequest.zip", gameDir.toString());
					log("Setting version number to " + onlineVersion);
					writeVersion(onlineVersion, dataManager.humanOnlineVersion, versionFile.getAbsolutePath());
					dataManager.downloadText = "Cleaning up";
					log("Cleaning up");
					new File(gameDir.toString() + slash + "edgequest.zip").delete();
					dataManager.downloadVisible = false;
					dataManager.downloadingState = 2;
					dataManager.downloadText = "Update Successful";
				}
			}
		} else {
			dataManager.downloadText = "Latest version installed";
		}
		
		getVersion(versionFile.getAbsolutePath());
		dataManager.launchText = "Version: " + dataManager.humanGameVersion;

		//If the launch button hasn't been an option, don't launch the game yet
		if (!dataManager.launchVisible) {
			dataManager.wml.launchGame = false;
		}
		dataManager.launchVisible = true;
		
		//Wait for user to press the launch button
		while (!dataManager.wml.launchGame) {
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//Start the game
		launchGame(gameDir + slash + "game");
		dataManager.running = false;
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);

	}
	private static void launchGame(String launchDir) {
		try {
			log("Launching with command: " + "java -Djava.library.path=" + launchDir + "/natives" + " -jar " + launchDir + "/edgequest.jar");
			Runtime.getRuntime().exec(new String[]{"java", "-Djava.library.path=" + launchDir + "/natives", "-jar", launchDir + "/edgequest.jar"});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static String setOS() {
		byte osByte = (byte) System.getProperty("os.name").toLowerCase().charAt(0);
		String userHome = System.getProperty("user.home");
		slash = "/";
		switch (osByte) {
		case 108:
			os = "linux";
			gameDir = Paths.get(userHome + "/.edgequest");
			break;
		case 109:
			os = "macOS";
			gameDir = Paths.get(userHome + "/Library/Application Support/edgequest");
			break;
		case 119:
			os = "windows";
			gameDir = Paths.get(userHome + "\\AppData\\Roaming\\edgequest");
			slash = "\\";
			break;
		default:
			os = "other";
		}
		return os;
	}
	private static int getVersion(String dir) {
		BufferedReader br = null;
		FileReader fr = null;
		try {

			fr = new FileReader(dir);
			br = new BufferedReader(fr);
			br = new BufferedReader(new FileReader(dir));
			int version = Integer.parseInt(br.readLine());
			try {
			dataManager.humanGameVersion = br.readLine();
			} catch (Exception e) {
				log("No human readable version found");
			}
			return version;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		return 0;
	}
	public static void writeVersion(int version, String human, String dir) {
		try{
			PrintWriter writer = new PrintWriter(dir, "UTF-8");
			writer.println(version);
			writer.println(human);
			writer.close();
		} catch (IOException e) {
			// do something
		}
	}
	private static int getOnlineVersion(String URLString) {
		URL url;
		InputStream is = null;
		BufferedReader br;
		try {
			url = new URL(URLString);
			is = url.openStream();  // throws an IOException
			br = new BufferedReader(new InputStreamReader(is));
			int val = Integer.parseInt(br.readLine());
			try {
			dataManager.humanOnlineVersion = br.readLine();
			} catch (Exception e) {
				log("Could not get human readable online version");
			}
			return val;
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
			} catch (IOException ioe) {
				// nothing to see here
			}
		}
		return -1;
	}
	private static boolean downloadLatest(String URLString, String saveDir) {
		try {
			URL website = new URL(URLString);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			@SuppressWarnings("resource")
			FileOutputStream fos = new FileOutputStream(saveDir);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private static void extractGame(String zipFile,String gameDir) { //black magic I got from http://stackoverflow.com/questions/9324933/what-is-a-good-java-library-to-zip-unzip-files
		try {
			int BUFFER = 2048;
			File file = new File(zipFile);
			@SuppressWarnings("resource")
			ZipFile zip = new ZipFile(file);
			String newPath = gameDir;
			new File(newPath).mkdir();
			@SuppressWarnings("rawtypes")
			Enumeration zipFileEntries = zip.entries();
			// Process each entry
			while (zipFileEntries.hasMoreElements()) {
				// grab a zip file entry
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				File destFile = new File(newPath, currentEntry);
				//destFile = new File(newPath, destFile.getName());
				File destinationParent = destFile.getParentFile();
				// create the parent directory structure if needed
				destinationParent.mkdirs();
				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zip
							.getInputStream(entry));
					int currentByte;
					// establish buffer for writing file
					byte data[] = new byte[BUFFER];
					// write the current file to disk
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos,BUFFER);
					// read and write until last byte is encountered
					while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void delete(File file) {
		try {
			if (file.isDirectory()) {
				//directory is empty, then delete it
				if (file.list().length==0) {
					file.delete();
				} else {
					//list all the directory contents
					String files[] = file.list();
					for (String temp : files) {
						//construct the file structure
						File fileDelete = new File(file, temp);
						//recursive delete
						delete(fileDelete);
					}
					//check the directory again, if empty then delete it
					if (file.list().length==0) {
						file.delete();
					}
				}

			} else {
				//if file, then delete it
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
