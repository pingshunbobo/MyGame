package GameServer;
import java.nio.ByteBuffer;

/*
 * 服务端处理进程。
 * 定义为静态方法，供多线程调用处理。
 *  
 * */

public class Work {

	public static void DataProcess(Conn conn){
		//如果数据未满，返回继续读！
		if(conn.bufin.position() < 6){
			System.out.println("read continue!");
			Server.ReadRegister(conn.sc);
			return;
		} else if(conn.bufin.position() >= 6){
			if(conn.user == null)
				UserLogin(conn);
			DataEcho(conn);
		} else{
			DataError(conn);
		}
		Server.WriteRegister(conn.sc);
	}
	
	private static void UserLogin(Conn conn){
		if(Login.check() == true)
			conn.user = new GameUser();
		conn.user.EchoId();
	}
	
	//简单把输入复制到输出。
	private static void DataEcho(Conn conn){
		ByteBuffer buf = conn.bufin;
		buf.flip();		//将buf内容做屏幕输出。
		while(buf.hasRemaining()){
			conn.bufout.put(buf.get());
		}
		buf.clear();
	}
	
	private static void DataError(Conn conn){
		conn.bufin.clear();
		conn.bufout.put("Error\r\n".getBytes());
	}
}
