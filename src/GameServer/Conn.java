/*
 * use hashmap storage user RemoteAddress -> data.
 * 
 * */

package GameServer;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Conn {
	
	SocketChannel sc = null;
	SocketAddress sa = null;
	ByteBuffer bufin = null;
	ByteBuffer bufout = null;
	ConnStatus status = null;

	GameUser user = null;
	
	//初始化一个连接结构。
	public Conn(Socket sock) {
		sa = sock.getRemoteSocketAddress();
    	bufin = ByteBuffer.allocate(1024);
    	bufout = ByteBuffer.allocate(1024);
    	
    	status = ConnStatus.READING;
    	
    	try {
        	this.sc = sock.getChannel();
			this.sc.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
	}
	
	//读取数据到用户空间。
	int ConnRead(){
    	int ReadBytes = -1;
		ByteBuffer buf = this.bufin;
		
		try {
			ReadBytes = this.sc.read(buf);
		} catch (IOException e) {
			Server.Debug_out(e.toString());
		}
		
		return ReadBytes;
    }
	
	//输出数据到socket。
    int ConnWrite(){
    	int bytewrites = -1;
    	
    	ByteBuffer buf = this.bufout;
    	buf.flip();
    	if(this.sc.isOpen()){
    		try {
    			bytewrites = this.sc.write(buf);
    		} catch (IOException e) {
    			Server.Debug_out(e.toString());
    		}
    	}
		buf.clear();
		return bytewrites;
    }
    
    //关闭Socket连接。
    void ConnClose() {
    	
		try {
			this.sc.close();
			Server.Connmap.remove(this.sa.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private enum ConnStatus{
		READING,PROCESSING,WRITEING,ERROR
	}
}
