package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.stenuit.xavier.hostsimulator.protocol.ParserException;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapMessage;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapParser;
import net.stenuit.xavier.tools.Converter;

public class ServiceRequest implements Runnable {
	private Socket socket;
	private Logger logger;
	private static final int SO_TIMEOUT=100; // how many milliseconds waiting for data on socket
	private static final int MAX_MSG_LEN=2000; // Maximum message size
	
	public ServiceRequest(Socket connection) {
        this.socket = connection;
        this.logger=Logger.getLogger("TCPServer");
    }
	
	@Override
	public void run() {
		try
		{
			/*
			BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			String line;
			while((line=br.readLine())!=null)
			{
				logger.log(Level.INFO, line);
				bw.write(""+line.length());
				bw.flush();
			}*/
			socket.setSoTimeout(SO_TIMEOUT);
			InputStream is=socket.getInputStream();
			OutputStream os=socket.getOutputStream();
			
			byte[] lentbl=new byte[4];
			try
			{
				is.read(new byte[2]); // 0x0A, 0x04
				is.read(lentbl);
				int len=((lentbl[0]&0xFF)<<24)+((lentbl[1]&0xFF)<<16)+((lentbl[2]&0xFF)<<8)+((lentbl[3]&0xFF));
				if(len>MAX_MSG_LEN)throw new ParserException("Message too long : "+len+" bytes");
				
				byte[] rawMsg=new byte[len+6];
				System.arraycopy(lentbl, 0, rawMsg, 2, 4);
				is.read(rawMsg,6,len);
				CtapParser p=new CtapParser();
				CtapMessage msg=(CtapMessage)p.parse(rawMsg);
				logger.log(Level.FINE, msg.dump());
				// System.out.println(msg.dump());
				
				byte[] msgtypeb=Converter.hex2bin(msg.getTag("F0.E1.D0"));
				String msgtype=new String(msgtypeb,"ASCII");
				switch(msgtype)
				{
					case "ci":
						logger.log(Level.INFO, "Received C-INQ");
						break;
					case "ri":
						logger.log(Level.INFO, "Received R-INQ");
						break;
					case "ct":
						logger.log(Level.INFO, "Received C-TRA");
						break;
					case "rt":
						logger.log(Level.INFO, "Received R-TRA");
						break;
					case "cb":
						logger.log(Level.INFO,"Received C-BAL");
						break;
					case "rb":
						logger.log(Level.INFO, "Received R-BAL");
						break;
					case "cp":
						logger.log(Level.INFO, "Received C-PAR");
						break;
					case "rp":
						logger.log(Level.INFO, "Received R-PAR");
						break;
					case "rX":
						logger.log(Level.INFO, "Received Rx");
						break;
					default:
						logger.log(Level.WARNING, "Received unknown message "+msgtype);
				}
			}
			catch(SocketTimeoutException e)
			{
				logger.log(Level.WARNING, "Timeout on socket");
			}
			catch(ParserException e)
			{
				logger.log(Level.WARNING, "Problem reading message : "+e.getMessage());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// the client closed
		}
		try{socket.close();}catch(IOException ioe){};
	}



}
