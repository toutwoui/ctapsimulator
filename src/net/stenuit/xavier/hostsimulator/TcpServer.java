package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class TcpServer {
	private static final int PORT=8079;
	private static final int NUMTHREADS=10;
	
	private ServerSocket serverSocket;
	private ExecutorService executorService=Executors.newFixedThreadPool(NUMTHREADS);
	private Logger logger=Logger.getLogger("TCPServer");
	
	private static TcpServer instance;
	
	public static void main(String[] args) {
		instance=new TcpServer();
		instance.runServer();
	}

	private void runServer() {
		System.out.println("java.util.logging.config.file="+System.getProperty("java.util.logging.config.file"));
		System.out.println("Number of handlers : "+logger.getHandlers().length);
		try
		{
			
			//Handler handler=new FileHandler("/tmp/mylog",true/*append*/);
			//SimpleFormatter sf=new SimpleFormatter();
			//handler.setFormatter(sf);
			//logger.addHandler(handler);
			//logger.setLevel(Level.FINE);
			
			LogManager logManager=LogManager.getLogManager();
			logManager.readConfiguration(getClass().getResourceAsStream("logging.properties"));
			

			logger.log(Level.SEVERE,"Starting Server");
			
			
			serverSocket=new ServerSocket(PORT);
			while(true)
			{
				try
				{
					System.out.println("Waiting for request");
					Socket s=serverSocket.accept();
					executorService.submit(new ServiceRequest(s)); // code in ServiceRequest will be executed in a new thread
					
					
				}
				catch(IOException e)
				{
					System.out.println("Error Accepting connexion");
					e.printStackTrace();
				}
			}
		}
		
		catch(IOException e)
		{
			logger.log(Level.SEVERE,"Could not start server on port "+PORT);
			e.printStackTrace();
		}
		
	}

}
