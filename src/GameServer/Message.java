package GameServer;

import java.nio.ByteBuffer;

public class Message {
	char iden;
	char type;
	int length;
	ByteBuffer body = null;
	MsgStatus status = null;
	public Message(){
		status = MsgStatus.READHEAD;
		body = ByteBuffer.allocate(1024);
	}
	
	public void SetIden(char id){
		iden = id;
	}
	
	public void SetType(char tp){
		type = tp;
	}
	
	public void SetText(String str){
		length = str.length();
		body.put(str.getBytes(),0 ,str.length());
	}
	
	public boolean ISREADING(){
		return(status == MsgStatus.READHEAD || status == MsgStatus.READBODY);
	}
	
	public boolean ISREADHEAD(){
		return(status == MsgStatus.READHEAD);
	}
	
	public boolean ISREADBODY(){
		return(status == MsgStatus.READBODY);
	}
	
	public boolean ISROCESSING(){
		return(status == MsgStatus.PROCESSING);
	}
	
	public boolean ISWRITEING(){
		return(status == MsgStatus.PROCESSING);
	}
	
	public boolean ISERROR(){
		return (status == MsgStatus.PROCESSING);
	}
	
	public void SETREADHEAD(){
		status = MsgStatus.READHEAD;
	}
	
	public void SETREADBODY(){
		status = MsgStatus.READBODY;
	}
	
	public void SETROCESSING(){
		status = MsgStatus.PROCESSING;
	}
	
	public void SETWRITEING(){
		status = MsgStatus.PROCESSING;
	}
	
	public void SETERROR(){
		status = MsgStatus.PROCESSING;
	}
	
	private enum MsgStatus{
		READHEAD, READBODY, PROCESSING, WRITEING, ERROR
	}
}
