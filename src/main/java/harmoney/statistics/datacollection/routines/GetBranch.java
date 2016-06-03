package harmoney.statistics.datacollection.routines;

import harmoney.statistics.web.StatisticsController;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetBranch extends MoneyBoxGet{

	private int branch;
	public GetBranch(String userName, String sessionId, int port,int branch) {
		super(userName, sessionId, port);
		this.branch = branch;
	}

	@Override
	public String getURI() {
		return "/harmoney2/branches/branch/" + branch;
	}
	
	final Logger logger = LoggerFactory.getLogger(GetBranch.class);
	
	public String getBranchName(){
		try {
			HttpResponse response = execute();
			StringBuffer buffer = getContent(response);
			
			JSONObject object = new JSONObject(buffer.toString());
			JSONObject data = (JSONObject)object.get("data");
			return (String)data.get("name");
		} catch (ClientProtocolException e) {
			logger.error("{}",e);
		} catch (IOException e) {
			logger.error("{}",e);
		}
		return "Unable to determine";
		
	}

}
