package net.vivekiyer.GAL;

/**
 * @author vivek
 * Class that is responsible for enabling or disabling
 * DEBUG messages
 */

public class Debug {
	
	// Set this to true to enable DEBUG messages
	public static boolean Enabled = true;;
	
	// StringBuffer that stores logs
	private static StringBuffer logger = new StringBuffer();
	
	public static void Log(String s){
		logger.append(s+"\n");
	}
	
	public static void printLog(){
		System.out.println(logger.toString());
		logger = new StringBuffer();
	}
	
}
