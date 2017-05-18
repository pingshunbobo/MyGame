package GameClient;

import java.net.*;
import java.io.*;

public class ClientMonitor {
	static int port = 30000;
	static String serverName = "127.0.0.1";
	
	public static void main(String[] args){
		
		//建立一个socket连接，并完成交互任务
		try {
			Socket conn = new Socket(serverName, port);
			TalkToServer(conn);
			conn.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void TalkToServer(Socket connsocket) throws IOException{
		
		//循环收发数据。
		while(true){
			//发送数据到服务端。
			OutputStream outToServer = connsocket.getOutputStream();
			DataOutputStream out =
					new DataOutputStream(outToServer);

			String data = "123456:\r\n";
			out.writeChar(0x2497);
			out.writeChar(0x01);
			out.writeInt(data.length());
			out.writeBytes(data);

			//接收来自服务端的数据
			InputStream inFromServer = connsocket.getInputStream();
			DataInputStream in =
					new DataInputStream(inFromServer);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					in , "utf-8"));
			System.out.println(br.readLine());
		}
	}
	
}

