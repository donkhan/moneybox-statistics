package harmoney.statistics.datacollection.routines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;

public class CustomerCollectionRoutine extends MoneyBoxGet{
	
	private int accountId;
	public CustomerCollectionRoutine(String userName,String sessionId,Integer accountId,int port){
		super(userName,sessionId,port);
		this.accountId = accountId;
	}
	
	@Override
	public String getURI() {
		return "/harmoney2/customers/find-by-account-id/" + accountId;
	}
		
	public StringBuffer getContent(HttpResponse response) throws IOException{
		InputStream is = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String s = "";
		while((s = reader.readLine()) != null){
			buffer.append(s);
		}
		reader.close();
		is.close();
		return buffer;
	}

	

}
