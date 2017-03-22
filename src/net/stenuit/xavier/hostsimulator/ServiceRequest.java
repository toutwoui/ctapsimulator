package net.stenuit.xavier.hostsimulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import net.stenuit.xavier.hostsimulator.props.HostSimulator;
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
			Log.warn("CPAR/RPAR exception : "+e.getMessage());
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
		rpar.setHeader(Converter.hex2bin("0A0400000000"));
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
		
		// ===Transaction Group===
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
		Element DF27=new Element("DF27",sdf.format(new Date()));
		Element DF2E=new Element("DF2E",host_incident_code);
		al=new ArrayList<Element>();
		al.add(DF27);
		al.add(DF2E);
		Element F5=new Element("F5",al);
		
		// ===Acquirer parameter group===
		al=new ArrayList<Element>();
		Element DF68=new Element("DF68",msg.getTag("F0.E2.FA.DF68")); // echo
		al.add(DF68);
		
		Element DF6B=new Element("DF6B","00000001");
		al.add(DF6B);
		
		Calendar c=Calendar.getInstance();
		if(TcpServer.hostSimulator.getUpdateFrequency()!=null)
		{ // use updateFrequency to add a value to current timestamp
			c.add(Calendar.SECOND,Integer.parseInt(TcpServer.hostSimulator.getUpdateFrequency()));
		}
		else
		{ // by default - next update time is 1 day
			c.add(Calendar.DATE,1);
		}
		Element DF63=new Element("DF63",sdf.format(c.getTime()));
		al.add(DF63);
		
		if(TcpServer.hostSimulator.getConnectionData()!=null)al.add(new Element("EE",TcpServer.hostSimulator.getConnectionData()));
		
		al.add(new Element("FE",TcpServer.hostSimulator.getCurrencyProfile()));
		
		Element DF814D=new Element("DF814D",TcpServer.hostSimulator.getAllowedTerminalMode());
		al.add(DF814D);
		
		Element DF8152=new Element("DF8152",TcpServer.hostSimulator.getOptionalDataElementSupport());
		al.add(DF8152);
		
		Element FA=new Element("FA",al);
		// ===End Acquirer Parameter Group
		
		// ===Card Brand Table===
		Element FB=null;
		if("0000".equals(host_incident_code))
		{
			// find all "brands" from input message
			Log.debug("input card brand table : "+msg.getTag("F0.E2.FB.EF"));
			// TODO : it seems that there are two forms possible there
			// FORM1 : only one brand : DF5F023003...
			// FORM2 : several brands : DF5F023003...DF5F023004....
			// only FORM1 is supported (single brand host simulator)
	
			Element DF5F=new Element("DF5F",msg.getTag("F0.E2.FB.EF.DF5F"));
			Element DF7F=new Element("DF7F",msg.getTag("F0.E2.FB.EF.DF7F")); //theorically, we should not echo
			if("00000000".equals(DF7F.getValue())) DF7F=new Element("DF7F","00000001"); // initial CPAR - terminal does not card brand parameter identifier
			Element T9F16=new Element("9F16",msg.getTag("F0.E2.FB.EF.9F16"));
			Element DF4F=new Element("DF4F",TcpServer.hostSimulator.getAllowedServices());
			Element DF42=new Element("DF42",TcpServer.hostSimulator.getAllowedCardEntryModes());
			Element ED=new Element("ED",TcpServer.hostSimulator.getCardBrandRiskManagement());
			Element DF3D=new Element("DF3D",TcpServer.hostSimulator.getCardholderVerificationModes());
			Element DF4E=new Element("DF4E",TcpServer.hostSimulator.getPinLengthType());
			
			al=new ArrayList<Element>();
			al.add(DF5F);
			al.add(DF7F);
			al.add(T9F16);
			al.add(DF2E); // will always be 0000 as of writing
			al.add(DF4F);
			al.add(DF42);
			al.add(ED);
			al.add(DF4E);
			Element EF=new Element("EF",al);
			al=new ArrayList<Element>();
			al.add(EF);
			FB=new Element("FB",al);
		}

		al=new ArrayList<Element>();
		al.add(F1);
		al.add(F5);
		al.add(FA);
		if(FB!=null)al.add(FB);
		Element E2=new Element("E2",al);
		// ====End Message Body====
		
		// === Message Footer===
		String f0e2=E2.rawDump();
		MessageDigest messageDigest;
		String cksum2="0000000000000000000000000000000000000000";
		try
		{
			messageDigest=MessageDigest.getInstance("SHA-1");
			messageDigest.reset();
			messageDigest.update(Converter.hex2bin(f0e2));
			cksum2=Converter.bin2hex(messageDigest.digest());
			
		}
		catch(Exception e)
		{
			Log.fatal("SHA-1 algorithm not found.  Please check your java installation");
		}
		Element DF8153=new Element("DF8153",cksum2);
		al=new ArrayList<Element>();
		al.add(DF8153);
		Element E3=new Element("E3",al);
		
		// === End Message Footer ===
		al=new ArrayList<Element>();
		al.add(E1);
		al.add(E2);
		al.add(E3);
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
