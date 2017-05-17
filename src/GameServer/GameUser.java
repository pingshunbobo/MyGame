package GameServer;

import java.util.UUID;

public class GameUser {
	UUID UserId;
	Conn conn;
	
	public GameUser(){
		UserId = UUID.randomUUID();
	}
	
	public void EchoId(){
		System.out.println("User id：" + UserId);
	}
}
