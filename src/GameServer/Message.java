package GameServer;

import java.nio.ByteBuffer;

public class Message {
	char iden;
	char type;
	int length;
	byte[] content = new byte[1024];
	
	public void SetIden(char id){
		iden = id;
	}
	
	public void SetType(char tp){
		type = tp;
	}
	
	public void SetText(String str){
		length = str.length();
		content = str.getBytes();
	}
	
	public ByteBuffer dump(){
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.putChar(iden);
		buf.putChar(type);
		buf.putInt(length);
		buf.put(content);
		return buf;
	}
}
