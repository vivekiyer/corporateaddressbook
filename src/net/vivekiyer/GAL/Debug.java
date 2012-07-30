package net.vivekiyer.GAL;

/**
 * @author vivek
 * Class that is responsible for enabling or disabling
 * DEBUG messages
 */

public class Debug {
	
	// Set this to true to enable DEBUG messages
	public static boolean Enabled = false;
	
	// StringBuffer that stores logs
	private static final StringBuffer logger = new StringBuffer();
	
	public static void Log(String s){
		logger.append(s+"\n");
	}
}
