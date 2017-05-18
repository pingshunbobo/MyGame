package GameServer;

import java.nio.ByteBuffer;

public class Message {
	char iden;
	char type;
	int length;
	String text;
	
	public void SetIden(char iden){
		this.iden = iden;
	}
	
	public void SetType(char tp){
		this.type = tp;
	}
	
	public void SetText(String str){
		this.length = str.length();
		this.text = str;
	}
	
	public ByteBuffer dump(){
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.putChar(iden);
		buf.putChar(type);
		buf.putInt(length);
		buf.put(text.getBytes());
		return buf;
	}
}
