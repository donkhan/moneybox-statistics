package harmoney.statistics.datacollection.routines;

import java.io.IOException;
import org.apache.http.HttpResponse;

public class MoneyBoxLogout extends MoneyBoxGet{

	@Override
	public String getURI() {
		return "/harmoney2/sessionService/logout";
	}
	
	public MoneyBoxLogout(String userName,String sessionId,String serverIP,int port){
		super(userName,sessionId,serverIP,port);
	}

	public void print(HttpResponse response) throws IOException{
		System.out.println("\nLogged Out");
	}

	
}
