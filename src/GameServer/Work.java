package GameServer;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

/*
 * 服务端处理进程。
 * 定义为静态方法，供多线程调用处理。
 *  
 * */

public class Work {

	public static void DataProcess(Conn conn){
		Message msg = GetMessage(conn);
		//如果数据头不完整。
		if(conn.bufin.position() < 8){
			DataContinue(conn);
			return;
		}
		
		//如果数据不完整，返回继续读！
		if(msg.type == 0x98){
			DataContinue(conn);
			return;
		} else if(msg.type == 0x99){
			DataError(conn);
		} else if(msg.type == 0x00){
			DataEcho(conn);
		} else if(msg.type == 0x01){
			UserLogin(conn);
		} else if(msg.type == 0x02){
			DataError(conn);
		} else if(msg.type == 0x03){
			DataError(conn);
		} else if(msg.type == 0x04){
			DataError(conn);
		};
		//无论如何，都会有处理结果产生。
		Server.WriteRegister(conn.sc);
	}
	
	private static void UserLogin(Conn conn){
		String RetStr = "Login Falure!\r\n";
		if(Login.check(GetMessage(conn)) == true){
			conn.user = new GameUser();
			conn.user.GetToken();
			RetStr = "Login Sucess!\r\n";
		}
		conn.bufout.put(RetStr.getBytes());
		conn.bufin.clear();
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
		System.out.println("read continue!");
		Server.ReadRegister(conn.sc);
	}
	
	private static void DataError(Conn conn){
		conn.bufin.clear();
		conn.bufout.put("Error\r\n".getBytes());
	}
	
	private static Message GetMessage(Conn conn){
		
		ByteBuffer Buffer = conn.bufin;
		Buffer.flip();
		
		Message msg = null;
		if(conn.msg == null){
			msg = new Message();
			//首先判断标识符。
			msg.iden = Buffer.getChar();
			if(msg.iden != 0x2497){
				msg.type = 0x99;
			}
			msg.type = Buffer.getChar();
			msg.length = Buffer.getInt();
			
		}else{
			msg = conn.msg;
	
			for(int i=0; i<msg.length; i++){
				if(Buffer.hasRemaining())
					msg.content[i] = Buffer.get();
				else{
					return null;
				}
			}
		}
		conn.msg = msg;
		return msg;
	}
}
