package GameServer;
import java.nio.ByteBuffer;

/*
 * 服务端处理进程。
 * 定义为静态方法，供多线程调用处理。
 *  
 * */

public class Work {

	public static void DataProcess(Conn conn){

		Message msg = GetMessage(conn);
		if(msg.ISREADING()){
			System.out.println("is reading!");
			//如果数据不完整，返回继续读！
			DataContinue(conn);
			return;
		}else if(msg.ISROCESSING()){
			if(msg.type == 0x00){
				DataEcho(conn);
			} else if(msg.type == 0x01){
				UserLogin(conn);
			} else if(msg.type == 0x02){
				DataError(conn);
			}
		}else if(msg.ISWRITEING()){
			DataEcho(conn);
		}else if(msg.ISERROR()){
			DataError(conn);
		}
		//无论如何，都会有处理结果产生。
		Server.WriteRegister(conn.sc);
	}
	
	//网络连接与用户的关联。
	private static void UserLogin(Conn conn){
		String RetStr = "Login Falure!\r\n";
		if(Login.check(GetMessage(conn)) == true){
			conn.user = new GameUser();
			conn.user.GetToken();
			RetStr = "Login Sucess!\r\n";
		}
		conn.bufout.put(RetStr.getBytes());
		conn.bufin.clear();
		conn.inmsg = null;
	}
	
	//简单把输入复制到输出。
	private static void DataEcho(Conn conn){
		ByteBuffer buf = conn.bufin;
		buf.flip();		//将buf内容做屏幕输出。
		while(buf.hasRemaining()){
			byte b = buf.get();
			conn.bufout.put(b);
		}
		buf.clear();
	}
	
	private static void DataContinue(Conn conn){
		Server.ReadRegister(conn.sc);
	}
	
	private static void DataError(Conn conn){
		conn.bufin.clear();
		conn.bufout.put("Error\r\n".getBytes());
	}
	
	private static Message GetMessage(Conn conn){
		Message msg = conn.inmsg;
		ByteBuffer Buffer = conn.bufin;
		
		if(msg == null ){
			msg = new Message();
		}
		
		//头部不完整，返回继续读取。
		if( msg.ISREADHEAD() && Buffer.position() < 8 ){
			return msg;
		}
		
		Buffer.flip();
		
		if(msg.ISREADHEAD()){
			
			msg.iden = Buffer.getChar();
			msg.type = Buffer.getChar();
			msg.length = Buffer.getInt();
			
			//首先判断标识符。
			if(msg.iden != 0x2497){
				msg.SETERROR();
			}
			msg.SETREADBODY();
			conn.inmsg = msg;
		}
		
		//继续读content部分
		if( msg.ISREADBODY() ){
			//取数据。
			while(Buffer.hasRemaining()){
				byte b = Buffer.get();
				msg.body.put(b);
				//如果完整的message。
				if(msg.body.position() == msg.length){
					msg.SETROCESSING();
					break;
				}
			}
		}
		Buffer.clear();
		return msg;
	}
}
