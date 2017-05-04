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
            InetSocketAddress address = new InetSocketAddress(30000);
        	listensocket.bind(address);
        	
            //向Selector中注册监听事件
			server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.out.println("Create bind port error!");
			e.printStackTrace();
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
				System.out.println("new event!");
				
				if(key.isAcceptable()) {
					Socket sock = SocketAccept(listensocket);
					String sockstr = sock.getRemoteSocketAddress().toString();
				    Conn newConn = new Conn(sock);
				    Connmap.put(sockstr, newConn);
				    
				} else if (key.isWritable()) {
					FindConn(key).ConnWrite();
					
				} else if (key.isReadable()) {
					FindConn(key).ConnRead();
					
				} else{
					//LOG.error();
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
    
    static  void NoticeProcesser(Conn Conn){
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
    
	private static void ThreadPool() {
		Thread WorkThread = null;
		// 开启8个ServerThread线程为该客户端服务。
        for(int i = 0; i < 8; i++) {
			WorkThread = new Thread(new ServerThread(i));
    		WorkThread.start();
        }
	}
}
