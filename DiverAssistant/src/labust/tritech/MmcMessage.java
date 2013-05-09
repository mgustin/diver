package labust.tritech;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MmcMessage 
{
	public int msgType;
	public int tx;
	public int rx;
	public int rxTmo;
	public byte[] payload;
	
	private static int header_size = 7;
	
	public byte[] serialize()
	{
		byte[] data = new byte[header_size + payload.length];
		data[0] = (byte) msgType;
		System.arraycopy(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)tx).array(), 0, data,1,2);
		System.arraycopy(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)rx).array(), 0, data,3,2);
		System.arraycopy(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)rxTmo).array(), 0, data,5,2);
		System.arraycopy(payload, 0, data,7,payload.length);
		
		return data;
	}
	
	public void deserialize(byte[] data)
	{
		msgType = data[0];
		tx = ByteBuffer.wrap(data,1,2).order(ByteOrder.LITTLE_ENDIAN).getShort();
		rx = ByteBuffer.wrap(data,3,2).order(ByteOrder.LITTLE_ENDIAN).getShort();
		rxTmo = ByteBuffer.wrap(data,5,2).order(ByteOrder.LITTLE_ENDIAN).getShort();
		payload = Arrays.copyOfRange(data, 7, data.length);
	}

}
