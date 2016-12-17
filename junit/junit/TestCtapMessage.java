package junit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.junit.Test;

import net.stenuit.xavier.hostsimulator.protocol.Element;
import net.stenuit.xavier.hostsimulator.protocol.ParserException;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapMessage;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapParser;
import net.stenuit.xavier.tools.Converter;

public class TestCtapMessage {

	byte[] realMessage=new byte[]{(byte)0x0A,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
			(byte)0xF0,(byte)0x82,(byte)0x01,(byte)0x3B,
			(byte)0xE1,(byte)0x0D,(byte)0xD0,(byte)0x02,(byte)0x63,(byte)0x74,(byte)0xD1,(byte)0x01,
			(byte)0x71,(byte)0xD3,(byte)0x04,(byte)0x02,(byte)0x20,(byte)0xF3,(byte)0xE8,(byte)0xE2,
			(byte)0x82,(byte)0x01,(byte)0x0E,(byte)0xF1,(byte)0x0B,(byte)0x9F,(byte)0x1C,(byte)0x08,
			(byte)0x33,(byte)0x31,(byte)0x33,(byte)0x39,(byte)0x30,(byte)0x39,(byte)0x35,(byte)0x39,
			(byte)0xF5,(byte)0x7C,(byte)0xDF,(byte)0x1E,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x07,
			(byte)0x86,(byte)0xDF,(byte)0x20,(byte)0x01,(byte)0x01,(byte)0xDF,(byte)0x21,(byte)0x02,
			(byte)0x88,(byte)0x10,(byte)0x5F,(byte)0x2A,(byte)0x02,(byte)0x09,(byte)0x78,(byte)0xDF,
			(byte)0x50,(byte)0x06,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,
			(byte)0xEA,(byte)0x10,(byte)0xEF,(byte)0x0E,(byte)0xDF,(byte)0x23,(byte)0x02,(byte)0x00,
			(byte)0x01,(byte)0xDF,(byte)0x24,(byte)0x06,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
			(byte)0x05,(byte)0x00,(byte)0x89,(byte)0x06,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,
			(byte)0x35,(byte)0x36,(byte)0xDF,(byte)0x27,(byte)0x07,(byte)0x20,(byte)0x16,(byte)0x12,
			(byte)0x05,(byte)0x16,(byte)0x16,(byte)0x09,(byte)0xEB,(byte)0x2E,(byte)0x57,(byte)0x13,
			(byte)0x60,(byte)0x60,(byte)0x77,(byte)0x38,(byte)0x27,(byte)0x52,(byte)0x56,(byte)0x07,
			(byte)0xD2,(byte)0x01,(byte)0x16,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
			(byte)0x00,(byte)0x00,(byte)0x0F,(byte)0x5A,(byte)0x08,(byte)0x60,(byte)0x60,(byte)0x77,
			(byte)0x38,(byte)0x27,(byte)0x52,(byte)0x56,(byte)0x07,(byte)0x5F,(byte)0x34,(byte)0x01,
			(byte)0x01,(byte)0x5F,(byte)0x24,(byte)0x03,(byte)0x20,(byte)0x11,(byte)0x30,(byte)0x5F,
			(byte)0x30,(byte)0x02,(byte)0x06,(byte)0x02,(byte)0xDF,(byte)0x2E,(byte)0x02,(byte)0x00,
			(byte)0x00,(byte)0xDF,(byte)0x2F,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0xF7,(byte)0x4F,
			(byte)0x84,(byte)0x06,(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x66,(byte)0x01,
			(byte)0x82,(byte)0x02,(byte)0x18,(byte)0x00,(byte)0x9F,(byte)0x02,(byte)0x06,(byte)0x00,
			(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x95,(byte)0x05,(byte)0x80,
			(byte)0x00,(byte)0x00,(byte)0x80,(byte)0x00,(byte)0x9A,(byte)0x03,(byte)0x16,(byte)0x12,
			(byte)0x05,(byte)0x9C,(byte)0x01,(byte)0x00,(byte)0x9F,(byte)0x36,(byte)0x02,(byte)0x00,
			(byte)0x13,(byte)0x9F,(byte)0x37,(byte)0x04,(byte)0xA0,(byte)0xAB,(byte)0xB3,(byte)0x1B,
			(byte)0x9F,(byte)0x26,(byte)0x08,(byte)0x7B,(byte)0xB1,(byte)0xD1,(byte)0x51,(byte)0x22,
			(byte)0xD6,(byte)0x9A,(byte)0x9F,(byte)0x9F,(byte)0x27,(byte)0x01,(byte)0x80,(byte)0x9F,
			(byte)0x10,(byte)0x07,(byte)0x06,(byte)0x01,(byte)0x0A,(byte)0x03,(byte)0xA4,(byte)0x00,
			(byte)0x00,(byte)0x9F,(byte)0x34,(byte)0x03,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0xFA,
			(byte)0x0E,(byte)0xDF,(byte)0x68,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,
			(byte)0xDF,(byte)0x6B,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x1C,(byte)0xFB,
			(byte)0x20,(byte)0xEF,(byte)0x1E,(byte)0xDF,(byte)0x5F,(byte)0x02,(byte)0x30,(byte)0x03,
			(byte)0xDF,(byte)0x7F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x1C,(byte)0x9F,
			(byte)0x16,(byte)0x0F,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,
			(byte)0x37,(byte)0x38,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
			(byte)0x00,(byte)0xE3,(byte)0x18,(byte)0xDF,(byte)0x81,(byte)0x53,(byte)0x14,(byte)0xE3,
			(byte)0x6E,(byte)0x66,(byte)0xEA,(byte)0x47,(byte)0xEA,(byte)0xD2,(byte)0xAA,(byte)0x2B,
			(byte)0xCC,(byte)0x81,(byte)0x2F,(byte)0x5A,(byte)0x4E,(byte)0x10,(byte)0xD7,(byte)0xED,
			(byte)0x84,(byte)0x1E,(byte)0x93};
	
	@Test
	public void testgetvalue() throws ParserException, NoSuchAlgorithmException
	{
		CtapParser p=new CtapParser();
		CtapMessage m=(CtapMessage)p.parse(realMessage);
		
		// leaf value
		System.out.println("Value of F0.E2.FA.DF6B : ");
		System.out.println(m.getTag("F0.E2.FA.DF6B"));
		assert("0000001C".equals(m.getTag("F0.E2.FA.DF6B")));
		
		// composite tag
		System.out.println("Value of F0.E2 : ");
		System.out.println(m.getTag("F0.E2"));
		assert("F10B9F1C083331333930393539F57CDF1E0400000786DF200101DF210288105F2A020978DF5006000000000500EA10EF0EDF23020001DF24060000000005008906313233343536DF270720161205161609EB2E57136060773827525607D20116020000000000000F5A0860607738275256075F3401015F24032011305F30020602DF2E020000DF2F020000F74F8406A00000026601820218009F0206000000000500950580000080009A031612059C01009F360200139F3704A0ABB31B9F26087BB1D15122D69A9F9F2701809F100706010A03A400009F3403010302FA0EDF680400000001DF6B040000001CFB20EF1EDF5F023003DF7F040000001C9F160F313233343536373800000000000000".equals(m.getTag("F0.E2")));
		String fullmsg=Converter.bin2hex(realMessage);
		assert(fullmsg.contains(m.getTag("F0")));
		
		System.out.println("Checksum in message(F0.E3.DF8153)");
		System.out.println(m.getTag("F0.E3.DF8153"));
		
		MessageDigest messageDigest=MessageDigest.getInstance("SHA-1");
		messageDigest.reset();
		messageDigest.update(Converter.hex2bin(m.getTag("F0.E2")));
		System.out.println("Computed checksum");
		System.out.println(Converter.bin2hex(messageDigest.digest()));
		
	}
	@Test
	public void testFindTags() throws ParserException
	{
		CtapParser p=new CtapParser();
		CtapMessage m=(CtapMessage)p.parse(realMessage);
		
		String[] tags;
		
		// finds intermediary tags
		tags=m.findTags("EF");
		System.out.println("tags found");
		for(String s:tags)System.out.println(s);
		assert("F0.E2.FB.EF".equals(tags[0]));
		
		// find single leaf tag
		tags=m.findTags("DF7F");
		System.out.println("tags found");
		for(String s:tags)System.out.println(s);
		assert("F0.E2.FB.EF.DF7F".equals(tags[0]));
		
		// find unexisting tags
		tags=m.findTags("A0ABB31B");
		System.out.println("tags found");
		assert(tags==null);
		System.out.println("null");
		
		// find lowercase tag
		tags=m.findTags("9f36");
		System.out.println("tags found");
		for(String s:tags)System.out.println(s);
		assert("F0.E2.F7.9F36".equals(tags[0]));
		
		// returns multiple result
		Element e1=new Element("E1","V1");
		Element e11=new Element("E1","V11");
		Element e2=new Element("E2","V2");
		ArrayList<Element> a1=new ArrayList<Element>();
		a1.add(e1);
		a1.add(e2);
		Element r1=new Element("R1",a1);
		ArrayList<Element> b1=new ArrayList<Element>();
		b1.add(e11);
		Element r2=new Element("R2",b1);
		ArrayList<Element> c1=new ArrayList<Element>();
		c1.add(r1);
		c1.add(r2);
		Element root=new Element("ROOT",c1);
		CtapMessage msg=new CtapMessage();
		msg.setRootElement(root);
		tags=msg.findTags("E1");
		System.out.println("tags found");
		int found=0;
		for(String s:tags)
		{
			System.out.println(s);
			if("ROOT.R1.E1".equals(s))found++;
			if("ROOT.R2.E1".equals(s))found++;
		}
		assert(found==2);
	}
	
	@Test
	public void testsetValue() throws ParserException
	{
		CtapParser p=new CtapParser();
		CtapMessage m=(CtapMessage)p.parse(realMessage);
		
		// replace an existing tag
		assert(!m.getTag("F0.E2.F1.9F1C").equals("12345678"));
		m.setTag("F0.E2.F1.9F1C", "12345678");
		assert(m.getTag("F0.E2.F1.9F1C").equals("12345678"));
		
		// add a non-existing tag as leaf of existing element
		assert(m.findTags("TOTO")==null);
		m.setTag("F0.E1.TOTO", "HELLO");
		assert(!(m.findTags("TOTO")==null));
		
		// add several non-existing tags
		assert(m.findTags("BROL")==null);
		m.setTag("F0.E1.BRIL.BROL", "taratata");
		System.out.println("After having added BRIL.BROL");
		System.out.println(m.dump());
		assert(!(m.findTags("BROL")==null));
		
		// trying to change root
		boolean exceptionRaised=false;
		try
		{
			m.setTag("F1.E1.BRIL.BROL", "value");
		}
		catch(IllegalArgumentException e){exceptionRaised=true;};
		assert(exceptionRaised);
	}
}
