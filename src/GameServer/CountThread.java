package GameServer;

//负责处理每个线程通信的线程类
public class CountThread implements Runnable
{
	public CountThread()
	{
		
	}
	
	public void run()
	{
		int old_count = 0;
		int new_count = 0;
		while(true){
			old_count = new_count;
			new_count = Server.process_count;
			
			System.out.println("process count: " + (new_count - old_count));
			System.out.println("requestconn count: " + Server.ConnProcessQueue.size());
			System.out.println("conn count: " + Server.Connmap.size());
			
			//停顿一秒
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}