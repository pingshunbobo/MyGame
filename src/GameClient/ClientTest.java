package GameClient;

public class ClientTest {
	static int port = 30000;
	static String serverName = null;
	
	public static void main(String [] args) {
		int connects = 0;
		int counts = 0;
		
		System.out.println("!!!Client test start!!!!");
		long startTime = System.currentTimeMillis();

		if(args.length != 3){
			System.out.println("Usage: ClientTest ServerIP connectors requests");
			return;
		}
		
		ClientTest.serverName = args[0];
		connects = Integer.parseInt(args[1]);
		counts = Integer.parseInt(args[2]);
		
		Thread[] WorkThread = new Thread[connects];

		//根据参数，建立线程组
        for(int i = 0; i < connects; i++){
    		WorkThread[i] = new Thread(new ClientThread(i, counts));
    		WorkThread[i].start();
        };
        
        //等待所有线程结束！
        for(int i=0; i<connects; i++){
        	try {
				WorkThread[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }  
        long endTime = System.currentTimeMillis();
        System.out.println("!!!CLIENT TEST OVER!!!");
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
	}
}