package google.code.asoap.util;

public class Log {
	
	public static void w(String context, String log)
	{
		log("Warning", context, log);
	}
	public static void e(String context, String log)
	{
		log("Error", context, log);
	}
	
	public static void i(String context, String log)
	{
		log("Info", context, log);
	}

	public static void d(String context, String log)
	{
		log("Debug", context, log);
	}
	
	private static void log(String type, String context, String log)
	{
		System.out.println("[" + type + "][" + context + "] " + log);
	}
}
