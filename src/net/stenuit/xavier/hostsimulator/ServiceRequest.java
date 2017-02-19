package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import net.stenuit.xavier.hostsimulator.props.Merchant;
import net.stenuit.xavier.hostsimulator.props.Terminal;
import net.stenuit.xavier.hostsimulator.protocol.Element;
import net.stenuit.xavier.hostsimulator.protocol.ParserException;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapMessage;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapParser;
import net.stenuit.xavier.tools.Converter;
import net.stenuit.xavier.tools.Log;

public class ServiceRequest implements Runnable {
	private Socket socket;
	private static final int SO_TIMEOUT=100; // how many milliseconds waiting for data on socket
	private static final int MAX_MSG_LEN=4096; // Maximum message size -usual value is 4096 according to CTAP.220
	
	public ServiceRequest(Socket connection) {
        this.socket = connection;
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
				Log.debug(line);
				bw.write(""+line.length());
				bw.flush();
			}*/
			socket.setSoTimeout(SO_TIMEOUT);
			InputStream is=socket.getInputStream();
			OutputStream os=socket.getOutputStream();
			
			byte[] lentbl=new byte[4];
			try
			{
				is.read(lentbl);
				int len=((lentbl[0]&0xFF)<<24)+((lentbl[1]&0xFF)<<16)+((lentbl[2]&0xFF)<<8)+((lentbl[3]&0xFF));
				if(len>MAX_MSG_LEN)
				{
					while(is.available()>0)
						is.read(); // evacuate all bytes in the pipe...
					throw new ParserException("Message too long : "+len+" bytes");
				}
				
				
				byte[] rawMsg=new byte[len];
				is.read(rawMsg);
				  
				
				// parses the input message to a CtapMessage structure
				CtapParser p=new CtapParser();
				CtapMessage msg=(CtapMessage)p.parse(rawMsg);
				Log.debug(msg.dump());
				
				
				byte[] msgtypeb=Converter.hex2bin(msg.getTag("F0.E1.D0"));
				String msgtype=new String(msgtypeb,"ASCII");
				// tags to be echoed
				String d1=msg.getTag("F0.E1.D1");
				String df1e=msg.getTag("F0.E2.F5.DF1E");
				String df20=msg.getTag("F0.E2.F5.DF20");
				String t9f1c=msg.getTag("F0.E2.F1.9F1C");
				String df68=msg.getTag("F0.E2.FA.DF68");
				
				CtapMessage retmsg;
				switch(msgtype)
				{
					case "ci":
						Log.info("Received C-INQ");
						// builds a RINQ message
						retmsg=(CtapMessage)p.parse(RINQ.rinq(d1));
						// adapts message with mandatory echoed tags
						if(df1e!=null)retmsg.setTag("F0.E2.F5.DF1E", df1e);
						if(df20!=null)retmsg.setTag("F0.E2.F5.DF20", df20);
						if(t9f1c!=null)retmsg.setTag("F0.E2.F1.9F1C", t9f1c);
						if(df68!=null)retmsg.setTag("F0.E2.FA.DF68", df68);
						// provides a random authorization code
						retmsg.setTag("F0.E2.F5.89", randomAuthCode());
						// computes checksum, and fills appropriate message
						retmsg.setTag("F0.E3.DF8153", retmsg.checksum());
						
						os.write(Converter.hex2bin(retmsg.rawDump()));
						Log.debug("Returned R-INQ: \n"+retmsg.dump());
						break;
					case "ri":
						Log.info("Received R-INQ");
						break;
					case "ct":
						Log.info("Received C-TRA");
						// builds a sandard RTRA message
						retmsg=(CtapMessage)p.parse(RTRA.rtra(d1));
						// sets the mandatory echoed tags
						if(df1e!=null)retmsg.setTag("F0.E2.F5.DF1E", df1e);
						if(df20!=null)retmsg.setTag("F0.E2.F5.DF20", df20);
						if(t9f1c!=null)retmsg.setTag("F0.E2.F1.9F1C", t9f1c);
						if(df68!=null)retmsg.setTag("F0.E2.FA.DF68", df68);
						// computes and sets checksum
						retmsg.setTag("F0.E3.DF8153", retmsg.checksum());
						os.write(Converter.hex2bin(retmsg.rawDump()));
						Log.debug("Returned R-TRA : \n"+retmsg.dump());
						break;
					case "rt":
						Log.info("Received R-TRA");
						break;
					case "cb":
						Log.info("Received C-BAL");
						break;
					case "rb":
						Log.info("Received R-BAL");
						break;
					case "cp":
						Log.info("Received C-PAR");
						retmsg=answerCpar(msg);
						os.write(Converter.hex2bin(retmsg.rawDump()));
						Log.debug("Returned : \n"+retmsg.dump());
						break;
					case "rp":
						Log.info("Received R-PAR");
						break;
					case "rX":
						Log.info("Received Rx");
						break;
					default:
						Log.error("Received unknown message "+msgtype);
				}
			}
			catch(SocketTimeoutException e)
			{
				Log.error("Timeout on socket");
			}
			catch(ParserException e)
			{
				Log.error("Problem reading message : "+e.getMessage());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// the client closed
		}
		try{socket.close();}catch(IOException ioe){};
	}

	/**
	 * Correctly answers CPAR message
	 * @return
	 */
	private CtapMessage answerCpar(CtapMessage msg) {
		CtapParser p=new CtapParser();
		try
		{
		// 1 check message type
		if(!"6370".equals(msg.getTag("F0.E1.D0")))throw new Exception("Message Type Identifier for C-PAR should be 'cp'");
		// 2 check mandatory fields
		final String[] mandatory={"F0.E1.D0","F0.E1.D1","F0.E1.D3","F0.E2.F1.9F1C","F0.E2.F1.DF04","F0.E2.F1.DF05","F0.E2.F1.9F1E",
				"F0.E2.F1.DF08","F0.E2.F1.9F1A","F0.E2.F1.9F33","F0.E2.F1.9F40","F0.E2.F1.9F35","F0.E2.F1.DF0A",
				"F0.E2.F2.DF12","F0.E2.F2.DF15","F0.E2.F2.DF16","F0.E2.F9.9F1C","F0.E2.F9.DF60","F0.E2.F9.DF62","F0.E2.F9.D4",
				"F0.E2.F9.E8"};
		
		for(String m:mandatory)
		{
			if(msg.getTag(m)==null)throw new Exception("Mandatory tag "+m+ "not found");
		}
		
		Log.debug("CPAR was valid - generating RPAR");
		
		return generateRpar(msg);
		}
		catch(Exception e)
		{ // in case of problem, answers with rX
			Log.warn("Rejected C-PAR : "+e.getMessage());
			CtapMessage ret=null;
			try
			{
				ret=(CtapMessage)p.parse(RX.rx("0002"));
			}
			catch(ParserException pe)
			{
				Log.error("Problem generating rX");
			}
			return ret;
		}
		
	}

	/**
	 * Generates a RPAR, based on a given CPAR
	 * @param msg
	 * @return
	 */
	private CtapMessage generateRpar(CtapMessage msg) {
		CtapMessage rpar=new CtapMessage();
		rpar.setHeader(Converter.hex2bin("A00400000000"));
		ArrayList<Element> al;
		String host_incident_code="0000";
		
		// if merchant does not exists -> host_incident_code=0001
		// if terminal does not exists -> host_incident_code=0002
		
		String m=msg.getTag("F0.E2.FB.EF.9F16").substring(0,20);
		Log.debug("MID from tag : >"+m+"<");
		String tid=new String(Converter.hex2bin(msg.getTag("F0.E2.F1.9F1C")));
		String mid=new String(Converter.removeTrailingZeroes(Converter.hex2bin(m)));
		boolean tidfound=false;
		boolean midfound=false;
		Log.debug("Searching TID:"+tid);
		Log.debug("and MID:"+mid);
		
		for(Merchant merchant:TcpServer.hostSimulator.getMerchants())
		{
			// 1234567890 with 
			// 1234567890
			Log.debug("Comparing "+merchant.getMerchantId()+" with "+mid);
			if(merchant.getMerchantId().equals(mid))
			{
				midfound=true;
				for(Terminal terminal:merchant.getTerminal())
				{
					Log.debug("Comparing "+terminal.getTerminalId()+" with "+tid);
					if(terminal.getTerminalId().equalsIgnoreCase(tid))
						tidfound=true;
				}
			}
		}
		
		Log.debug("Tid found:"+tidfound);
		Log.debug("Mid found:"+midfound);
		if(!tidfound)host_incident_code="0002";
		if(!midfound)host_incident_code="0003";
		
		
		// Build answer
		// ====Message Header====
		Element D0=new Element("D0",Converter.bin2hex("rp".getBytes()));
		Element D1=new Element("D1",msg.getTag("F0.E1.D1"));
		Element D2=new Element("D2",msg.getTag("F0.E1.D2"));
		al=new ArrayList<Element>();
		al.add(D0);
		al.add(D1);
		al.add(D2);
		Element E1=new Element("E1",al);
		
		// ====Message Body====
		// ===Terminal Group===
		Element tag_9f1c=new Element("9F1C",msg.getTag("F0.E2.F1.9F1C"));
		// TODO support for "request for action"
		al=new ArrayList<Element>();
		al.add(tag_9f1c);
		Element F1=new Element("F1",al);
		al=new ArrayList<Element>();
		al.add(F1);
		Element E2=new Element("E2",al);
		
		// ===Transaction Group===
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		Element DF27=new Element("DF27",sdf.format(new Date()));
		Element DF2E=new Element("DF2E",host_incident_code);
		al=new ArrayList<Element>();
		al.add(DF27);
		al.add(DF2E);
		Element F5=new Element("F5",al);
		
		// ===
		
		al=new ArrayList<Element>();
		al.add(E1);
		al.add(E2);
		al.add(F5);
		
		Element F0=new Element("F0",al);
		
		rpar.setRootElement(F0);
		
		Log.info(rpar.dump());
		return rpar;
	}

	/* REturns a 6 digit random number ASCII code (0x30,0x34,0x32,....)
	 * - used for random authcode
	 */
	private String randomAuthCode() {
		int r=new Random().nextInt(1000000);
		
		String ret="";
		for(int i=0;i<6;i++)
		{
			ret=ret+"3"+r%10;
			r=r/10;
		}
		return ret;
	}



}
