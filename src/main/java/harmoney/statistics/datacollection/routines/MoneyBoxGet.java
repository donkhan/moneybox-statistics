package harmoney.statistics.datacollection.routines;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class MoneyBoxGet {

	private String server = "localhost";
	private int port = 9192;

	protected String getServer() {
		return server;
	}

	protected int getPort() {
		return port;
	}

	public HttpResponse execute() throws ClientProtocolException,
			IOException {
		HttpClient client = new DefaultHttpClient();
		String url = "http://" + getServer() + ":" + getPort() + getURI();
		HttpGet request = new HttpGet(url);

		request.addHeader("Content-Type", "application/json;charset=UTF-8");
		request.addHeader("User-Agent", "STATISTICS-SERVER");
		request.addHeader("Accept", "application/json");
		addExtraHeaders(request);
		HttpResponse response = client.execute(request);
		return response;
	}

	public abstract String getURI();

	private String userName;
	private String sessionId;
	
	public MoneyBoxGet(String userName,String sessionId){
		this.userName = userName;
		this.sessionId = sessionId;
	}

	
	public void addExtraHeaders(HttpGet request){
		request.addHeader("X-userId",userName);
		request.addHeader("Cookie","JSESSIONID="+sessionId);
	}
	
}
