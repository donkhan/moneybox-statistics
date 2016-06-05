package harmoney.statistics.model;

public class Credentials {

	private String userName;
	private String password;
	private int port;
	private String serverIP;
	private int registrationServerPort;
	
	public int getRegistrationServerPort() {
		return registrationServerPort;
	}
	public void setRegistrationServerPort(int registrationServerPort) {
		this.registrationServerPort = registrationServerPort;
	}
	public String getServerIP() {
		return serverIP;
	}
	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String toString(){
		return "User Name : " + userName + " Password " + password + " Port " + port;
	}
}
