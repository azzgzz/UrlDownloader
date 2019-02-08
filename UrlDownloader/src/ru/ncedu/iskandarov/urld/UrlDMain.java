package ru.ncedu.iskandarov.urld;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlDMain {

	private static String url = null;
	private static String path = "";
	private static boolean toOpen = false;
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		
		UrlDownloader ud = new UrlDownloaderImpl();
		if (args.length == 0) {
			System.out.print("Enter as cmd line argument");
			return;
		}
		
		if(parseCmdLineArgs(args)) {
			System.out.println("Enter url as first argument, path as second argument (optional)"
					+ " and \"-open\" to open url after saving");
			return;
		}
		
		ud.setUrl(url);
		ud.setPath(path);
		
		try {
			ud.save();
		} catch (IllegalStateException e) {
			System.out.println("You must call setUrl first");
		} catch (SecurityException e) {
			System.out.println("Security issues...");
		} catch (IOException e) {
			System.out.println("Can`t download this Url. Maybe it wrong");
		} catch (Exception e) {
			System.out.println("OK");
			return;
		}
		
		if (toOpen)
			ud.open();
	}

	
	private static boolean parseCmdLineArgs(String[] args) {
		
		switch(args.length) {
		case 0:
			return true;
		case 1: {
			url = args[0];
			try {
				new URL(url);
			} catch (MalformedURLException e) {
				return true;
			}
			return false;
		}
		case 2:
			if (args[1].equals("-open")) {
				toOpen = true;
			} else {
				path = args[1];
			}
			break;
		case 3:
			if (args[2].equals("-open")) {
				path = args[1];
				toOpen = true;
			} else if(args[1].equals("-open")) {
				path = args[2];
				toOpen = true;
			} else {
				System.out.println("FATAL ERROR: wrong command line arguments!");
				return true;
			}
			break;
		default:
			System.out.println("FATAL ERROR: too many command line arguments!");
			return true;
		}
		url = args[0];
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return true;
		}
		return false;
	}

}
