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
		//如果数据未满，返回继续读！
		if(msg == null){
			System.out.println("read continue!");
			Server.ReadRegister(conn.sc);
			return;
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
		if(Login.check(GetMessage(conn)) == true)
			conn.user = new GameUser();
		conn.user.EchoId();
	}
	
	//简单把输入复制到输出。
	private static void DataEcho(Conn conn){
		ByteBuffer buf = conn.bufin;
		buf.flip();		//将buf内容做屏幕输出。
		while(buf.hasRemaining()){
			byte b = buf.get();
			conn.bufout.put(b);
			System.out.print((char)b);
		}
		buf.clear();
	}
	
	private static void DataError(Conn conn){
		conn.bufin.clear();
		conn.bufout.put("Error\r\n".getBytes());
	}
	
	private static Message GetMessage(Conn conn){
		if(conn.bufin.position() < 8)
			return null;
		Message msg = new Message();
		ByteBuffer dupeBuffer = conn.bufin.duplicate();
		msg.iden = dupeBuffer.getChar();
		msg.type = dupeBuffer.getChar();
		msg.length = dupeBuffer.getInt();
		if(msg.iden != 0x2497){
			System.out.print((int)msg.iden);
			return null;
		}
		return msg;
	}
}
