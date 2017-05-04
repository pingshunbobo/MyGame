package GameClient;

import java.net.*;
import java.util.Random;
import java.io.*;

public class Client {
	int ClientId = 0 ;
	int request_count = 0;
	
	public Client(int id, int counts) {
		this.ClientId = id;
		this.request_count = counts;
	}
	
	public void work(){
		try {
			Random random = new Random();
			Thread.sleep(random.nextInt(65535)%10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//建立一个socket连接，并完成交互任务
		try {
			Socket conn = new Socket(ClientTest.serverName, ClientTest.port);
			this.TalkToServer(conn);
			conn.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void TalkToServer(Socket connsocket) throws IOException{
		
		//循环收发数据。
		for(int i = 0; i < this.request_count; i++){
			//发送数据到服务端。
			OutputStream outToServer = connsocket.getOutputStream();
			DataOutputStream out =
					new DataOutputStream(outToServer);
			out.writeBytes( "abcd\r\n");

			//接收来自服务端的数据
			InputStream inFromServer = connsocket.getInputStream();
			DataInputStream in =
					new DataInputStream(inFromServer);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					in , "utf-8"));
		}
	}
	
}

