package labust.tritech;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MtMessage 
{
	public int size, txNode, rxNode, byteCount, msgType, seq, node;
	public byte[] payload;
	
	private static int header_size = 8;
	private static int count_diff=5;
	
	static public byte[] serializeHH(MtMessage msg)
	{
		int len = header_size + msg.payload.length;
		
		byte[] data = new byte[len + 5];
		data[0] = '@';
		System.arraycopy(String.format("%04x",len).toUpperCase().getBytes(),0,data,1,4);
		byte[] msgd = msg.serialize();
		System.arraycopy(msgd,0,data,5,msgd.length);
		
		return data;
	}
	
	public byte[] serialize()
	{
		size = header_size + payload.length;
		byte[] data = new byte[header_size + payload.length];
		System.arraycopy(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)size).array(), 0, data,0,2);
		data[2] = (byte) txNode;
		data[3] = (byte) rxNode;
		data[4] = (byte) (size - count_diff);
		data[5] = (byte) msgType;
		data[6] = (byte) 128;
		data[7] = (byte) node;
		System.arraycopy(payload, 0,data,8,payload.length);
		
		return data;
	}
	
	public void deserialize(byte[] data)
	{
		size = ByteBuffer.wrap(data,0,2).order(ByteOrder.LITTLE_ENDIAN).getShort();
		
		txNode = data[2];
		rxNode = data[3];
		byteCount = data[4];
		msgType = data[5];
		seq = data[6];
		node = data[7];
		payload = Arrays.copyOfRange(data, 8, data.length);
	}
}
