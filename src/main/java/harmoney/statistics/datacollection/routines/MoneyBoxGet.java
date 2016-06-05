package harmoney.statistics.datacollection.routines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public abstract class MoneyBoxGet {
	
	protected String getServer(){
		return serverIP;
	}
	protected int getPort(){
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
	
	private int port;
	private String serverIP;
	
	public MoneyBoxGet(String userName,String sessionId,String serverIP,int port){
		this.userName = userName;
		this.sessionId = sessionId;
		this.port = port;
		this.serverIP = serverIP;
	}

	
	public void addExtraHeaders(HttpGet request){
		request.addHeader("X-userId",userName);
		request.addHeader("Cookie","JSESSIONID="+sessionId);
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
