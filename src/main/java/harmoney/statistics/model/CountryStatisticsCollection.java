package harmoney.statistics.model;

import java.util.ArrayList;
import java.util.List;

public class CountryStatisticsCollection {
	
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
}
