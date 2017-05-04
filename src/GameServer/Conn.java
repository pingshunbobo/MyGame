/*
 * use hashmap storage user RemoteAddress -> data.
 * 
 * */

package GameServer;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Conn {
	SocketChannel sc = null;
	SocketAddress sa = null;
	ByteBuffer bufin = null;
	ByteBuffer bufout = null;
	ConnStatus status = null;

	//初始化一个连接结构。
	public Conn(Socket sock) {
		sa = sock.getRemoteSocketAddress();
    	bufin = ByteBuffer.allocate(1024);
    	bufout = ByteBuffer.allocate(1024);
    	
    	status = ConnStatus.READING;
    	
    	try {
        	this.sc = sock.getChannel();
			this.sc.configureBlocking(false);
		    this.ReadRegister();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
	}
	
	//读取数据到用户空间。
	int ConnRead(){
    	int ReadBytes = 0;
		ByteBuffer buf = this.bufin;
		
		try {
			ReadBytes = this.sc.read(buf);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		//判断返回值，注册事件
		if( ReadBytes > 0 ){
			this.NoRegister();
			Server.NoticeProcesser(this);
		} else if(ReadBytes < 0){
			this.ConnClose();
		}else{
			//Debug_out("buf reamain: " + this.bufin.remaining());
		}
		return ReadBytes;
    }
	
	//输出数据到socket。
    int ConnWrite(){
    	int bytewrites = 0;
    	
    	ByteBuffer buf = this.bufout;
    	buf.flip();
    	if(this.sc.isOpen()){
    		try {
    			bytewrites = this.sc.write(buf);
    			this.ReadRegister();
    		} catch (IOException e) {
    			this.ConnClose();
    		}
    	}
		buf.clear();
		return bytewrites;
    }
    
    //关闭Socket连接。
    void ConnClose() {
		try {
			this.CancelRegister();
			this.sc.close();
			Server.Connmap.remove(this.sa.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	public void DataProcess(){
		Server.process_count ++;
		//如果数据未满，返回继续读！
		if(bufin.position() < 6){
			this.ReadRegister();
			return;
		} else if(bufin.position() >= 6){
			this.DataEcho();
		} else{
			this.DataError();
		}
		this.WriteRegister();
	}
	
	//简单把输入复制到输出。
	private void DataEcho(){
		ByteBuffer buf = this.bufin;
		buf.flip();				//将buf内容做屏幕输出。
		while(buf.hasRemaining()){
			this.bufout.put(buf.get());
		}
		buf.clear();
	}
	
	private void DataError(){
		this.bufin.clear();
		this.bufout.put("Error\r\n".getBytes());
	}
	
	public void ReadRegister(){
		try{
			if(this.sc.isConnected())
				this.sc.register(Server.selector, SelectionKey.OP_READ);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void WriteRegister(){
		try{
			if(!this.sc.socket().isClosed())
				this.sc.register(Server.selector, SelectionKey.OP_WRITE);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public void NoRegister(){
		try{
			if(!this.sc.socket().isClosed())
				this.sc.register(Server.selector, 0);
		}
		catch(IOException e){
			e.printStackTrace();
		};
	}
	public void CancelRegister(){
		if(this.sc.isOpen())
			this.sc.keyFor(Server.selector).cancel();
	}
	
	private enum ConnStatus{
		READING,PROCESSING,WRITEING,ERROR
	}
}
