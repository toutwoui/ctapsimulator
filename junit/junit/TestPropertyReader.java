package junit;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import net.stenuit.xavier.hostsimulator.props.HostSimulator;

public class TestPropertyReader {
	private static final String myXml="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
"<HostSimulator>"+
    
        "<merchant>"+
          "<merchantId>1012456789</merchantId>"+
          "<terminal>"+
            "<terminalId>str1234</terminalId>"+
          "</terminal>"+
          "<terminal>"+
            "<terminalId>str1234</terminalId>"+
          "</terminal>"+
          "<terminal>"+
            "<terminalId>str1234</terminalId>"+
          "</terminal>"+
        "</merchant>"+
    
  	"<card>"+
      "<pan>1234567890123456</pan>"+
      "<expirityDate>1231</expirityDate>"+
      "<sequence>000</sequence>"+
    "</card>"+
    "<card>"+
      "<pan>str1234</pan>"+
      "<expirityDate>str1234</expirityDate>"+
      "<sequence>str1234</sequence>"+
      "<available>123</available>"+
      "<returnCode>123</returnCode>"+
    "</card>"+
  
"</HostSimulator>";
	
	@Test
	public void testUnmashall() throws JAXBException
	{
		JAXBContext jaxbContext=JAXBContext.newInstance(HostSimulator.class);
		Unmarshaller jaxbUnmarshaller=jaxbContext.createUnmarshaller();
		HostSimulator prop=(HostSimulator)jaxbUnmarshaller.unmarshal(new StringReader(myXml));
		
		
		System.out.println("MerchantId of first first merchant of first acquirer : "+prop.getMerchants().get(0).getMerchantId());
		assert("1012456789".equals(prop.getMerchants().get(0).getMerchantId()));
		System.out.println("First PAN : "+prop.getCards().get(0).getPan());
		assert("1234567890123456".equals(prop.getCards().get(0).getPan()));
	}
}
