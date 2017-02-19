package junit;

import org.junit.Test;

import net.stenuit.xavier.tools.Converter;

public class TestConverter {
	@Test
	public void testRemoveTrailing()
	{
		byte[] in=new byte[]{(byte)0x12,(byte)0x00,(byte)0x34,(byte)0x80,(byte)0x00,(byte)0x00,(byte)0x00};
		byte[] out=Converter.removeTrailingZeroes(in);
		assert(out.length==4);
		assert(out[3]==(byte)0x80);
	}
}
