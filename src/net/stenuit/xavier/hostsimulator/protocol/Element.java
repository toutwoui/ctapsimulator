package net.stenuit.xavier.hostsimulator.protocol;

import java.util.ArrayList;
import java.util.List;

import net.stenuit.xavier.tools.Converter;

public class Element {
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public boolean hasSubElements()
	{
		if(subElements==null)return false;
		return subElements.size()>=1;
	}
	public List<Element> getSubElements()
	{
		return subElements;
	}
	public void addSubElement(Element toadd) throws IllegalArgumentException
	{
		if(value!=null)throw new IllegalArgumentException("Error : attempt to add a subelement to a non-composite element");
		subElements.add(toadd);
	}
	/**
	 * Remove an element from a composite element
	 * @param nameToRemove name of the element to remove
	 * @return true if the element was found and has been removed
	 * @throws IllegalArgumentException if you attempted to remove a subelement from a non-composite element
	 */
	public boolean removeSubElement(String nameToRemove) throws IllegalArgumentException
	{
		boolean ret=false;
		if(value!=null)throw new IllegalArgumentException("Error : attempt to remove a subelement from a non-composite element");
		for(Element test:subElements)
		{
			if(test.name.equalsIgnoreCase(nameToRemove))
			{
				ret=true;
				subElements.remove(test);
			}
		}
		return ret;
	}
	/* either value or subelements, but never both */
	private String value;
	private ArrayList<Element> subElements;
	private String name;
	private int len;
	
	public Element(String name,String value)
	{
		if(name==null)throw new IllegalArgumentException("name should not be null");
		if(value==null)throw new IllegalArgumentException("value should not be null");
		this.name=name;
		this.value=value;
		this.len=value.length()/2;
		this.subElements=null;
	}
	
	public Element(String name,List<Element> subElements)
	{
		if(name==null)throw new IllegalArgumentException("name should not be null");
		if(subElements==null)throw new IllegalArgumentException("subElements should not be null");
		this.name=name;
		this.value=null;
		this.subElements=new ArrayList<Element>();
		this.len=0;
		for(Element subElement:subElements)
		{
			this.subElements.add(subElement);
			this.len+=subElement.getName().length()/2; // length of "tag"
			this.len+=subElement.getLen(); // Length of [composite] value
			this.len+=1; // length of "len" - minimal 1
			if(subElement.getLen()>127)this.len++; // length of "len" =2 if > 127 bytes
			if(subElement.getLen()>255)this.len++; // length of "len" =3 if > 255 bytes
		}
	}
	
	public String rawDump()
	{
		String ret="";
		
		if(this.value!=null)
		{ // simple tag
			ret+=value;
		}
		else
		{ // compound tag
			for(Element subElement:subElements)
			{
				ret+=subElement.getName();
				// ret+="["+subElement.getLen()+"]";
				if(subElement.getLen()<=127)
				{
					ret+=Converter.bin2hex(new byte[]{(byte)subElement.getLen()});
				}
				else if(subElement.getLen()<=255)
				{
					ret+="81";
					ret+=Converter.bin2hex(new byte[]{(byte)subElement.getLen()});
				}
				else if(subElement.getLen()<=65535)
				{
					ret+="82";
					ret+=Converter.bin2hex(new byte[]{(byte)(subElement.getLen()>>8)});
					ret+=Converter.bin2hex(new byte[]{(byte)(subElement.getLen()&0xFF)});
				}
				ret+=subElement.rawDump();
			}
		}
		return ret;
	}
	/** dumps an element in human-readable form - prints the name, followed by '=', followed by value
	 * 
	 * @param prefix what to print before dumping element (used to show the tag hierarchy)
	 * 
	 * @return what to print
	 */
	public String dump(String prefix)
	{
		String ret="";
		if(prefix==null)prefix="";
		
		if(this.value!=null)
		{ // simple tag
			ret+=prefix;
			if(!"".equals(prefix))
				ret+=".";
			ret+=name+"=";
			ret+=value;
		}
		else
		{ // compound tag
			if(!"".equals(prefix))
			{
				prefix+="."+name;
			}
			else
				prefix=name;
			
			for(Element subElement:subElements)
			{
				ret+=subElement.dump(prefix);
				ret+="\n";
			}
		}
		return ret;
	}

	public int getLen() {
		return len;
	}
}
