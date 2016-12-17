package net.stenuit.xavier.hostsimulator.protocol.ctap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.stenuit.xavier.hostsimulator.protocol.Element;
import net.stenuit.xavier.hostsimulator.protocol.Message;
import net.stenuit.xavier.tools.Converter;

public class CtapMessage extends Message {
	private byte[] header; // all bytes before first tag
	private Element rootElement;
	
	public byte[] getHeader() {
		return header;
	}
	public void setHeader(byte[] header) {
		if(!isComplete)
			this.header = header;
	}
	public Element getRootElement() {
		return rootElement;
	}
	public void setRootElement(Element rootElement) {
		if(!isComplete)
			this.rootElement = rootElement;
	}
	@Override
	public String dump() {
		String ret;
		ret="HDR:"+Converter.bin2hex(header);
		ret+="\n";
		ret+=rootElement.dump("");
		return(ret);
	}
	
	/**
	 * Finds all occurences of given tag
	 * @param tagname tag to find (for example "9F1C")
	 * @return List of all found tags, with their hierarchy ("F0.E2.F1.9F1C") or null if tag not found
	 */
	public String[] findTags(String tagname)
	{
		ArrayList<String> ret;
		ret=recurFind(new ArrayList<String>(),getRootElement(),tagname,"");
		if(ret.size()==0) return null;
		return Arrays.asList(ret.toArray()).toArray(new String[ret.size()]);
	}
	
	private ArrayList<String> recurFind(ArrayList<String> templist,Element fromElm,String tagToFind,String path)
	{
		ArrayList<String> ret=templist;
		if(fromElm.hasSubElements())
		{
			if(tagToFind.equalsIgnoreCase(fromElm.getName()))
			{
				ret.add(path+"."+fromElm.getName());
			}
			else
			{
				if(!"".equals(path))path=path+".";
				path=path+fromElm.getName();	
				for(Element e:fromElm.getSubElements())
				{
					recurFind(ret,e,tagToFind,path);
				}
			}
			
		}
		else if(tagToFind.equalsIgnoreCase(fromElm.getName()))
		{
			ret.add(path+"."+fromElm.getName());
		}
		return ret;
	}
	
	/**
	 * reads value of a tag
	 * if tag is constructed, the full tag is returned with its subtags 
	 * 
	 * @param toget tag to get e.g. F0.E1.9F1C
	 * @return value of the tag or null if not found
	 */
	public String getTag(String toget)
	{
		Element e=getRootElement();
		StringTokenizer st=new StringTokenizer(toget,".");
		String s;
		s=st.nextToken();
		
		while(e.getName().equalsIgnoreCase(s))
		{
			try{s=st.nextToken();}catch(NoSuchElementException ex){return e.rawDump();};
			List<Element> l=e.getSubElements();
			for(Element el:l)
			{
				if(el.getName().equalsIgnoreCase(s))
				{
					e=el;
					break;
				}
			}
		}
		return null;
	}
	
	/**
	 * Set a value in a non-composite tag. The root must match, if any intermediate tag is not found, it will be created on the fly
	 * 
	 * @param tag full path of the tag (e.g. F0.E1.D0)
	 * @param value String value of the tag
	 * @throws IllegalArgumentException
	 */
	public void setTag(String tag,String value) throws IllegalArgumentException
	{
		if(tag==null||value==null)throw new IllegalArgumentException("setTag called with null argument(s)");
		if(!tag.startsWith(getRootElement().getName())) throw new IllegalArgumentException("root element does not match");
		Element e=getRootElement();
		StringTokenizer st=new StringTokenizer(tag,".");
		String s;
		s=st.nextToken();
		
		while(e.getName().equalsIgnoreCase(s))
		{
			try{s=st.nextToken();}catch(NoSuchElementException ex){e.setValue(value);return;}; // reached the end of the tree --> change value
			
			List<Element> l=e.getSubElements(); // digging in the tree
			boolean found=false;
			for(Element el:l)
			{
				if(el.getName().equalsIgnoreCase(s))
				{
					e=el;
					found=true;
					break;
				}
			}
			
			if(!found)
			{
				// looked in all tags, but could not find a suitable one... create it
				if(tag.endsWith(s))
				{ // all tree except last element has been traversed --> create value
					Element newel=new Element(s,value);
					e.addSubElement(newel);
					return;
				}
				else
				{ // creating intermediate tag and enters it
					Element newel=new Element(s,new ArrayList<Element>());
					e.addSubElement(newel);
					e=newel;
					continue;
				}
			}
			
			
		}
		// At this point, the insertion failed
	}
}
