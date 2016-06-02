package harmoney.statistics.model;

public class CountryStatistics {

	private double totalBuyValue = 0;
	public double getTotalBuyValue() {
		return totalBuyValue;
	}
	public void setTotalBuyValue(double totalBuyValue) {
		this.totalBuyValue = totalBuyValue;
	}
	public double getTotalSellValue() {
		return totalSellValue;
	}
	public void setTotalSellValue(double totalSellValue) {
		this.totalSellValue = totalSellValue;
	}
	public double getTotalBuy() {
		return totalBuy;
	}
	public void setTotalBuy(double totalBuy) {
		this.totalBuy = totalBuy;
	}
	public double getTotalSell() {
		return totalSell;
	}
	public void setTotalSell(double totalSell) {
		this.totalSell = totalSell;
	}
	private double totalSellValue = 0;
	private String country = "";
	private double totalBuy = 0;
	private double totalSell = 0;
	
	private double buyPercentage = 0;
	private double sellPercentage = 0;
	private double buyValuePercentage = 0;

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
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}

	
	
	
	
}
