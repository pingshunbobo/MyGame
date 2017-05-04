package GameClient;

//负责处理每个线程通信的线程类
public class ClientThread implements Runnable
{
	int thread_id = 0;
	int request_counts = 0;
	
	public ClientThread(int id, int times)
	{
		this.thread_id = id;
		this.request_counts = times;
	}
	
	public void run()
	{
		Client cli = new Client(thread_id, request_counts);
		cli.work();
	}
	
}