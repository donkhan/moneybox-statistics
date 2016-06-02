package harmoney.statistics.model;

public class CountryStatistics {

	private double totalBuyAmount;
	private double totalSellAmount;
	private String country;
	private int totalBuys;
	private int totalSells;
	
	private double buyPercentage;
	private double sellPercentage;
	private double buyValuePercentage;

	public double getBuyPercentage() {
		return buyPercentage;
	}
	public void setBuyPercentage(double buyPercentage) {
		this.buyPercentage = buyPercentage;
	}
	public double getSellPercentage() {
		return sellPercentage;
	}
	public void setSellPercentage(double sellPercentage) {
		this.sellPercentage = sellPercentage;
	}
	public double getBuyValuePercentage() {
		return buyValuePercentage;
	}
	public void setBuyValuePercentage(double buyValuePercentage) {
		this.buyValuePercentage = buyValuePercentage;
	}
	public double getSellValuePercentage() {
		return sellValuePercentage;
	}
	public void setSellValuePercentage(double sellValuePercentage) {
		this.sellValuePercentage = sellValuePercentage;
	}
	private double sellValuePercentage;

	
	public double getTotalBuyAmount() {
		return totalBuyAmount;
	}
	public void setTotalBuyAmount(double totalBuyAmount) {
		this.totalBuyAmount = totalBuyAmount;
	}
	public double getTotalSellAmount() {
		return totalSellAmount;
	}
	public void setTotalSellAmount(double totalSellAmount) {
		this.totalSellAmount = totalSellAmount;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public int getTotalBuys() {
		return totalBuys;
	}
	public void setTotalBuys(int totalBuys) {
		this.totalBuys = totalBuys;
	}
	public int getTotalSells() {
		return totalSells;
	}
	public void setTotalSells(int totalSells) {
		this.totalSells = totalSells;
	}
	
	
}
