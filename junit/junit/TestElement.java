package junit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import junit.framework.AssertionFailedError;
import net.stenuit.xavier.hostsimulator.protocol.Element;

public class TestElement {

	@Test
	public void testElementStringString() {
		Element newElement=new Element("name","value");
		assertNotNull(newElement);
		
		boolean success=false;
		try{
			newElement=new Element(null,"value");
		}
		catch(IllegalArgumentException e)
		{
			success=true;
		}
		assertTrue(success);
		
		success=false;
		try{
			newElement=new Element("name",(String)null);
		}
		catch(IllegalArgumentException e)
		{
			success=true;
		}
		assertTrue(success);
	}

	@Test
	public void testElementStringListOfElement() {
		Element element1=new Element("name","value");
		Element element2=new Element("name","value");
		ArrayList<Element> al=new ArrayList<Element>();
		al.add(element1);
		al.add(element2);
		
		Element newElement=new Element("name",al);
		assertNotNull(newElement);
		
		boolean success=false;
		try{
			newElement=new Element(null,al);
		}
		catch(IllegalArgumentException e)
		{
			success=true;
		}
		assertTrue(success);
		
		success=false;
		try{
			newElement=new Element("name",(ArrayList<Element>)null);
		}
		catch(IllegalArgumentException e)
		{
			success=true;
		}
		assertTrue(success);
		
	}

	@Test
	public void testElementEdition()
	{
		Element element1=new Element("E1","V1");
		Element element2=new Element("E2","V2");
		ArrayList<Element> al=new ArrayList<Element>();
		al.add(element1);
		al.add(element2);
		Element newElement=new Element("E0",al);
		
		// hasSubElements
		assert(!element1.hasSubElements());
		assert(!element2.hasSubElements());
		assert(newElement.hasSubElements());
		
		Element element3=new Element("E3","V3");
		newElement.addSubElement(element3);
		assert(newElement.removeSubElement("E2"));
		
		System.out.println("dump:");
		System.out.println(newElement.dump(""));
		
		assert("E0.E1=V1\nE0.E3=V3\n".equals(newElement.dump("")));
		
	}
	@Test
	public void testDump() {
		Element element1=new Element("E1","V1");
		Element element2=new Element("E2","V2");
		ArrayList<Element> al=new ArrayList<Element>();
		al.add(element1);
		al.add(element2);
		Element newElement=new Element("E0",al);
		
		String dump=newElement.dump(null);
		System.out.println("dump:\n"+dump);
		String asserted="E0.E1=V1\nE0.E2=V2\n";
		if(!asserted.equals(dump))
		{
			fail("dump not working :\n"+dump);
		}
	}

}
