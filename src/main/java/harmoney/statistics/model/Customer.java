package harmoney.statistics.model;

public class Customer {
	
	String name;
	String country;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		if(country == null || country.equals("null")){
			country = "UNKNOWN";
		}
		this.country = country;
	}
	
	public String toString(){
		return "Name : " + name + ", Country : " + country;
	}
}
