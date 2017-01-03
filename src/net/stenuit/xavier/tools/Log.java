package net.stenuit.xavier.tools;

// java.util.logging
// import java.util.logging.LogManager;
// import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
			//java.util.logging
			//logger=Logger.getLogger(LN);
			//LogManager logManager=LogManager.getLogManager();
			//Object o=new Log();
			//logManager.readConfiguration(o.getClass().getResourceAsStream("logging.properties"));
			
			// Apache log4j
			logger=LogManager.getLogger(LN);
			
			
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
		// java.util.logging
		// logger.severe(msg);
		logger.fatal(msg);
	}
	public static synchronized void error(String msg)
	{
		// java.util.logging
		// logger.warning(msg);
		logger.error(msg);
	}
	public static synchronized void debug(String msg)
	{
		// java.util.logging
		// logger.fine(msg);
		logger.debug(msg);
	}
	public static synchronized void warn(String msg)
	{
		logger.warn(msg);
	}
}
