package GameServer;

import java.util.UUID;

public class GameUser {
	UUID Token;
	Conn conn;
	
	public GameUser(){
		Token = UUID.randomUUID();
	}
	
	public void GetToken(){
		System.out.println("User id：" + Token);
	}
}
