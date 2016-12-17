package net.stenuit.xavier.hostsimulator.protocol.ctap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import net.stenuit.xavier.hostsimulator.protocol.Element;
import net.stenuit.xavier.hostsimulator.protocol.Message;
import net.stenuit.xavier.hostsimulator.protocol.Parser;
import net.stenuit.xavier.hostsimulator.protocol.ParserException;
import net.stenuit.xavier.tools.Converter;

public class CtapParser extends Parser {
	/**
	 * hashmap of tags (key being tag, value giving format)
	 * 
	 * for example tag.get("F0") would return "c" (for composed)
	 * tag.get("9F1C") would return "8i"
	 * 
	 * Parsing the message would be easy :
	 * 1/ 
	 * - get one byte, and look for tag
	 * - if not found, get second byte and look for tag
	 * - if not found, get third byte and look for tag
	 * - if not found ParseException
	 * 2/
	 * - get length (+ sanity check on length)
	 * 3/
	 * - get value
	 */
	private HashMap<String, String> ctapTags;	
	byte[] rawData;
	int pos=0;
	
	public CtapParser()
	{
		// reads tags file
		InputStream is=this.getClass().getResourceAsStream("tags"); 
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
		String s;
		try
		{
			ctapTags=new HashMap<String,String>();
			while((s=br.readLine())!=null)
			{
				if(s.startsWith("#"))continue;// skips comments
				if(s.equals(""))continue;// skips empty lines
				StringTokenizer st=new StringTokenizer(s,",");
				String tag=st.nextToken();
				/*String name=*/st.nextToken();
				String format=st.nextToken();
				ctapTags.put(tag,format);
			}
		}
		catch(IOException ioe){}
		finally
		{
			try{br.close();}catch(Exception e){};
			try{is.close();}catch(Exception e){};
		}
		
	}
	@Override
	public Message parse(byte[] in) throws ParserException {
		rawData=in;
		pos=0;
		
		CtapMessage ret=new CtapMessage();
		
		// Header before first tag
		byte[] hdr=new byte[6]; // TODO change with CTAP header length
		System.arraycopy(rawData, pos, hdr, 0, 6);
		pos+=6;
		ret.setHeader(hdr);
		ret.setRootElement(parseTag(null));
		
		return ret;
	}

	@Override
	public Message parse(InputStream in) throws ParserException {
		// TODO Auto-generated method stub
		return null;
	}

	/** will read from rawData the next Tag, it length and its value
	 * Then, it creates an element, and links it to the parent (if any)
	 * If the tag is composite, it will recursively call the method to get a valid result
	 * 
	 * @param parent the parent tag, or null if no parent
	 * 
	 * @return the topmost element (parent...)
	 * @throws ParserException if we could not parse
	 */
	private Element parseTag(Element parent) throws ParserException
	{
		byte[] tag;
		String tagstr=null;
		int len=0;
		boolean found=false;
		Element elm;
	
		for(int i=0;i<3;i++)
		{
			tag=new byte[i+1];
			System.arraycopy(rawData, pos, tag, 0, i+1);
			tagstr=Converter.bin2hex(tag);
			
			if(ctapTags.get(tagstr)!=null)
			{
				found=true;
				pos+=i+1;
				break;
			}
			
		}
		if(!found)throw new ParserException("could not parse "+pos+"of message : "+Converter.bin2hex(rawData));
		len=decodeLength();
		
		if("c".equals(ctapTags.get(tagstr)))
		{ // coumpound tag
			ArrayList<Element> elmList=new ArrayList<Element>();
			int nexttagpos=pos+len;
			while(pos<nexttagpos)
			{
				Element e2=parseTag(null);
				elmList.add(e2);
			}
			elm=new Element(tagstr,elmList);
		}
		else
		{ // simple tag
			byte[] val=new byte[len];
			System.arraycopy(rawData, pos, val, 0, len);
			pos+=len;
			
			elm=new Element(tagstr,Converter.bin2hex(val));
		}
		
		return elm;
	}
	private int decodeLength() throws ParserException {
		byte b1=rawData[pos++];
		byte b2=0;
		byte b3=0;
		
		if(b1<0)
		{ // there's a second byte
			b1=(byte)(b1&(0x007F)); //mask one bit
			b2=rawData[pos++];
		}
		else
			return (int)b1;
		
		if(b1==1) // b1 contains 1, b2 contains len
			return (int)b2;
		
		if(b1==2) // b1 contains 2, b2 and b3 contains len
		{
			b3=rawData[pos++];
			return((int)b2<<8)|((int)b3);
		}
		throw new ParserException("could not parse tag if its length is "+b1+" bytes long !");
	}
}
