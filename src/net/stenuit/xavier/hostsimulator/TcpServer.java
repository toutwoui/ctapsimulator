package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.stenuit.xavier.tools.Log;


public class TcpServer {
	private static final int PORT=8079;
	private static final int NUMTHREADS=10;
	
	private ServerSocket serverSocket;
	private ExecutorService executorService=Executors.newFixedThreadPool(NUMTHREADS);
	
	private static TcpServer instance;
	
	public static void main(String[] args) {
		instance=new TcpServer();
		instance.runServer();
	}

	private void runServer() {
		try
		{
			

			Log.fatal("Starting Server");
			
			
			serverSocket=new ServerSocket(PORT);
			while(true)
			{
				try
				{
					Log.info("Waiting for request");
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
			Log.fatal("Could not start server on port "+PORT);
			e.printStackTrace();
		}
		
	}

}
