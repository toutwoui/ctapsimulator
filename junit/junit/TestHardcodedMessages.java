package junit;

import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

import net.stenuit.xavier.hostsimulator.CINQ;
import net.stenuit.xavier.hostsimulator.CTRA;
import net.stenuit.xavier.hostsimulator.RINQ;
import net.stenuit.xavier.hostsimulator.RTRA;
import net.stenuit.xavier.hostsimulator.protocol.ParserException;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapMessage;
import net.stenuit.xavier.hostsimulator.protocol.ctap.CtapParser;

public class TestHardcodedMessages {

	@Test
	public void testCINQ() {
		CtapParser p=new CtapParser();
		byte[] cinq=CINQ.cinq();
		try
		{
			p.parse(cinq);
		}
		catch(ParserException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRINQ()
	{
		CtapParser p=new CtapParser();
		byte[] rinq=RINQ.rinq();
		byte[] rinq2=RINQ.rinq("3C"); // D1=33
		try
		{
			System.out.println(p.parse(rinq).dump());
			CtapMessage m=(CtapMessage)p.parse(rinq2);
			System.out.println(m.dump());
			assert("3C".equals(m.getTag("F0.E1.D1")));
		}
		catch(ParserException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testCTRA() {
		CtapParser p=new CtapParser();
		byte[] ctra=CTRA.ctra();
		try
		{
			p.parse(ctra);
		}
		catch(ParserException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRTRA()
	{
		CtapParser p=new CtapParser();
		byte[] rtra=RTRA.rtra();
		byte[] rtra2=RTRA.rtra("3C"); // D1=3C
		try
		{
			System.out.println(p.parse(rtra).dump());
			CtapMessage m=(CtapMessage)p.parse(rtra2);
			System.out.println(m.dump());
			assert("3C".equals(m.getTag("F0.E1.D1")));
		}
		catch(ParserException e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
