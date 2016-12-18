package net.stenuit.xavier.tools;

import java.util.logging.LogManager;
import java.util.logging.Logger;



/** implements log system based either on java.util.logging
 * 
 * @author xs
 *
 */
public class Log {
	private static final String LN="STDLOG"; // Logger Name
	
	private static Logger logger;
	static
	{
		try
		{
			logger=Logger.getLogger(LN);
			LogManager logManager=LogManager.getLogManager();
			Object o=new Log();
			logManager.readConfiguration(o.getClass().getResourceAsStream("logging.properties"));
		}
		catch(Exception e){
			System.out.println("Could not initialize logger module");
			e.printStackTrace();
		};

	}
	
	public static synchronized void info(String msg)
	{
		logger.info(msg);
	}
	public static synchronized void fatal(String msg)
	{
		logger.severe(msg);
	}
	public static synchronized void error(String msg)
	{
		logger.warning(msg);
	}
	public static synchronized void debug(String msg)
	{
		logger.fine(msg);
	}
}
