package harmoney.statistics.model;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class SessionMap extends HashMap<String,JSONObject> {
	
	private static final long serialVersionUID = 1L;
	private static SessionMap sessionMap;
	private static Map<String,String> reverseMap = new HashMap<String,String>();
	private SessionMap(){
		super();
	}
	
	public static SessionMap getSessionMap(){
		if(sessionMap == null){
			sessionMap = new SessionMap();
		}
		return sessionMap;
	}
	
	public JSONObject put(String sessionId,JSONObject jsonContent){
		String name = (String)jsonContent.get("id");
		if(reverseMap.containsKey(name)){
			String id = reverseMap.get(name);
			remove(id);
		}
		reverseMap.put(name,sessionId);
		return super.put(sessionId,jsonContent);
	}
}
