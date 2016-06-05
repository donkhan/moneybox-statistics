package harmoney.statistics.server;

import harmoney.statistics.model.SessionMap;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RegistrationServer {
	private final static int PACKETSIZE = 10000;
	
	final Logger logger = LoggerFactory.getLogger(RegistrationServer.class);
	private JSONParser parser = new JSONParser();
	private int port = 3332;
	
	public void start() {
		try {
			DatagramSocket socket = new DatagramSocket(port);
			logger.info("Registration Server is ready and listening in port " + port);
			for (;;) {
				DatagramPacket packet = new DatagramPacket(	new byte[PACKETSIZE], PACKETSIZE);
				socket.receive(packet);
				String data = new String(packet.getData());
				JSONObject jsonContent = getJSONObject(data.trim());
				InetAddress IPAddress = packet.getAddress();
                int receivedPort = packet.getPort();
                String token = UUID.randomUUID().toString();
                SessionMap.getSessionMap().put(token,jsonContent);
                DatagramPacket sendPacket =  new DatagramPacket(token.getBytes(), token.getBytes().length,
                		IPAddress, receivedPort);
                socket.send(sendPacket);
                
			}
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private JSONObject getJSONObject(String message){
		try {
			JSONObject jsonObject = (JSONObject)parser.parse(message);
			return jsonObject;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}