/*
 * main thread.
 * 负责所有的 tcp 连接 和socket io
 * 
 * */

package GameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Server {
	
	static int debug = 1;
	static int port = 30000;
	
	//TCP 多路监听连接
	static Selector selector;
    static ServerSocket listensocket = null;
    static ServerSocketChannel server = null;
	
	//Hash表存储核心数据
	static HashMap< String , Conn> Connmap =
		      new HashMap< String, Conn>();

	//任务队列，供线程池异步接受任务
	static Queue <Conn> ConnProcessQueue
		= new LinkedList<Conn>();
	
    public static void main(String[] args){
        try {
        	//主线程中开启一个选择器，用于监听多事件。
			selector = Selector.open();
        	
			//创建线程池
			ThreadPool();
	        
			//创建套接字连接
        	server = ServerSocketChannel.open();
	        server.configureBlocking(false);
	        
    		//绑定通道到指定端口 
            listensocket = server.socket();
            InetSocketAddress address = new InetSocketAddress(port);
        	listensocket.bind(address);
        	
            //向Selector中注册监听事件
			server.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("开始监听端口：" + port + "......");
		} catch (IOException e) {
			System.out.println("Create bind port error!");
		}
        
        //进入io循环服务，不再返回。
        IOService();
        
        System.out.println("Can not run to here!");
        
	}//End of main function.
	
    //主线程中负责所有的io服务。 
    private static void IOService(){
        while(true){
			//阻塞等待。
			SocketSelect();
	        
			Set <SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator <SelectionKey> keyIterator = selectedKeys.iterator();
			
			while(keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				
				if(key.isAcceptable()) {
					Socket sock = SocketAccept(listensocket);
					String sockstr = sock.getRemoteSocketAddress().toString();
				    Conn newConn = new Conn(sock);
				    Connmap.put(sockstr, newConn);
				    
				    ReadRegister(newConn.sc);
				    
				    DebugOut("New connect!");
				    
				} else if (key.isWritable()) {
					DebugOut("New Write event!");
					WriteProcess(FindConn(key));
					
				} else if (key.isReadable()) {
					DebugOut("New Read event!");
					
					ReadProcess(FindConn(key));
					
				} else{
					DebugOut("Unknow event happend!");
				}
				keyIterator.remove();
			}
		}// End of while
    }
    
	//对select 函数的封装。
	static void SocketSelect(){
		try {
			selector.select(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//对accept函数的封装。
	static Socket SocketAccept(ServerSocket listensocket){
		Socket sock = null;
		try {
			sock = listensocket.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sock;
	}
    
	//创建线程池。
	private static void ThreadPool() {
		Thread WorkThread = null;
		// 开启8个ServerThread线程为该客户端服务。
        for(int i = 0; i < 8; i++) {
			WorkThread = new Thread(new ServerThread(i));
    		WorkThread.start();
        }
	}
	
    static void NoticeWorkThread(Conn Conn){
    	DebugOut("Notice event!");
    	//加入处理队列,交由线程池处理。
		synchronized(ConnProcessQueue){
			ConnProcessQueue.offer(Conn);
			ConnProcessQueue.notify();
		}
    }
    
    //ͨ通过SelectionKey找到对应的Conn全局表数据。
    private static Conn FindConn(SelectionKey key){
    	SocketAddress sa = null;
		try {
			sa = ((SocketChannel) key.channel()).getRemoteAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return Connmap.get(sa.toString());
    }
    
    
    //处理读事件。
    private static void ReadProcess(Conn ioconn){
    	int ret = ioconn.ConnRead();
		//判断返回值，注册事件
		if( ret > 0 ){
			//处理过程中，不接收io事件。
			ClearRegister(ioconn.sc);
			NoticeWorkThread(ioconn);
		} else if(ret < 0){
			DebugOut("Read error, Close socket!");
			CloseProcess(ioconn);
		}else{
			//可能是缓冲区满。
			DebugOut("buf reamain: " + ioconn.bufin.remaining());
		}
    }
    
    //处理写事件。
    private static void WriteProcess(Conn ioconn){
		int ret = ioconn.ConnWrite();
		if(ret < 0)
			CloseProcess(ioconn);
		else
			ReadRegister(ioconn.sc);
    }
    
    //conn关闭事件。
    private static void CloseProcess(Conn ioconn){
    	/*
    	 * 这里应该添加业务层下线登记。
    	 * 
    	 * */
    	
		CancelRegister(ioconn.sc);
		ioconn.ConnClose();
    }
    
	public static void ReadRegister(SocketChannel sc){
		try{
			if(sc.isConnected())
				sc.register(selector, SelectionKey.OP_READ);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void WriteRegister(SocketChannel sc){
		try{
			if(!sc.socket().isClosed())
				sc.register(selector, SelectionKey.OP_WRITE);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public static void ClearRegister(SocketChannel sc){
		try{
			if(!sc.socket().isClosed())
				sc.register(selector, 0);
		}
		catch(IOException e){
			e.printStackTrace();
		};
	}
	public static void CancelRegister(SocketChannel sc){
		if(sc.isOpen())
			sc.keyFor(Server.selector).cancel();
	}
	
	public static void DebugOut(String str){
		if(Server.debug > 0)
			System.out.println(str);
	}
}
