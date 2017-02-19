package net.stenuit.xavier.hostsimulator;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.stenuit.xavier.hostsimulator.props.HostSimulator;
import net.stenuit.xavier.tools.Log;


public class TcpServer {
	private static final int DEFAULT_PORT=8079;
	private static int port;
	private static String configFileName=null;
	public static HostSimulator hostSimulator;
	
	private static final int NUMTHREADS=10;
	
	private ServerSocket serverSocket;
	private ExecutorService executorService=Executors.newFixedThreadPool(NUMTHREADS);
	
	private static TcpServer instance;
	

	public static void main(String[] args) {
		port=DEFAULT_PORT;
		
		int max=args.length;
		for(int i=0;i<max;i++)
		{
			if("-p".equals(args[i])) // -p port
				port=Integer.parseInt(args[++i]);
			else if("-c".equals(args[i])) // -c configFileName
				configFileName=args[++i];
			else
			{
				System.err.println("Usage : TcpServer [-p port] [-c configFileName]");
				System.exit(1);
			}
		}
		instance=new TcpServer();
		instance.runServer();
	}

	private void runServer() {
		try
		{
			Log.fatal("Starting Server");
			if(configFileName!=null)
			{
				try
				{
					JAXBContext jaxbContext=JAXBContext.newInstance(HostSimulator.class);
					Unmarshaller jaxbUnmarshaller=jaxbContext.createUnmarshaller();
					hostSimulator=(HostSimulator)jaxbUnmarshaller.unmarshal(new FileReader(configFileName));
					Log.info("Successfully parsed config filename : "+configFileName);
				}
				catch(JAXBException jxb)
				{
					Log.printStackTrace(jxb);
					System.exit(1);
				}
			}
			
			
			serverSocket=new ServerSocket(port);
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
			Log.fatal("Could not start server on port "+DEFAULT_PORT);
			e.printStackTrace();
		}
		
	}

}
