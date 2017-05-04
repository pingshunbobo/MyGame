package GameServer;

//负责处理每个线程通信的线程类
public class ServerThread implements Runnable
{
	int Thread_id = 0;
	public ServerThread(int id)
	{
		this.Thread_id = id;
	}
	
	public void run()
	{
		Conn connecter = null;
		while(true){
			synchronized(Server.ConnProcessQueue){
				if( 0 == Server.ConnProcessQueue.size() )
					try {
						//等待队列中的消息，并取得控制权。
						Server.ConnProcessQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
				}
				connecter = Server.ConnProcessQueue.poll();
			}
			//正式处理客户请求。
			if(connecter != null)
				synchronized(connecter){
						connecter.DataProcess();
				}
		}
	}
	
}