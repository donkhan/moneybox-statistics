package harmoney.statistics.datacollection.routines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

public class MoneyBoxLogin extends MoneyBoxPost{

	private String userName;
	private String passWord;
	private int port;
	
	public MoneyBoxLogin(String userName,String passWord,int port){
		this.userName = userName;
		this.passWord = passWord;
		this.port = port;
	}
	
	@Override
	public String getPayLoad() {
		JSONObject user = new JSONObject();
		user.accumulate("id",userName);
		user.accumulate("password",passWord);
		return user.toString();
	}

	
	public String login() throws ClientProtocolException, IOException{
		HttpResponse response = execute();
		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		Header[] headers = response.getAllHeaders();
		for(Header header : headers){
			if(header.getName().equals("Set-Cookie")){
				String tokenValue = header.getValue();
				String x = tokenValue.substring(tokenValue.indexOf("=")+1,tokenValue.indexOf(";"));
				return x;
			}
		}
		return "";
	}

	@Override
	public String getURI() {
		return "/harmoney2/sessionService/authenticate";
	}

	@Override
	public void addExtraHeaders(HttpPost request) {

	}

	@Override
	protected int getPort() {
		return port;
	}
}
