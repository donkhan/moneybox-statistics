package harmoney.statistics.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

public class CountryStatisticsCollection implements JRDataSource {
	
	private List<CountryStatistics> items = new ArrayList<CountryStatistics>();

	public List<CountryStatistics> getItems() {
		return items;
	}

	public void setItems(List<CountryStatistics> items) {
		this.items = items;
	}
	
	public void addItem(CountryStatistics item){
		this.items.add(item);
	}
	
	@Override
	public Object getFieldValue(JRField field) throws JRException {
		String name = field.getName();
		if(name.equals("index")){
			return pointer + 1;
		}
		CountryStatistics item = items.get(pointer);
		if(name.equals("countryName")){
			return item.getCountry();
		}
		
		if(name.equals("totalBuy")){
			return item.getTotalBuy();
		}
		
		if(name.equals("totalBuyPercentage")){
			return item.getBuyPercentage();
		}
		
		if(name.equals("totalBuyValue")){
			return item.getTotalBuyValue();
		}
		
		if(name.equals("totalBuyValuePercentage")){
			return item.getBuyValuePercentage();
		}
		
		if(name.equals("totalSell")){
			return item.getTotalSell();
		}
		
		if(name.equals("totalSellPercentage")){
			return item.getSellPercentage();
		}
		
		if(name.equals("totalSellValue")){
			return item.getTotalSellValue();
		}
		
		if(name.equals("totalSellValuePercentage")){
			return item.getSellValuePercentage();
		}
		
		return "";
	}

	@Override
	public boolean next() throws JRException {
		return ++pointer < items.size();
	}
	

	private int pointer = -1;
	public CountryStatisticsCollection(){
		pointer = -1;
	}
}
