package harmoney.statistics.datacollection.routines;

import harmoney.statistics.model.Credentials;


public class CounterTransactionsRetrievalRoutine extends MoneyBoxGet{
	
	public CounterTransactionsRetrievalRoutine(Credentials credentials,String sessionId){
		super(credentials.getUserName(), sessionId, credentials.getServerIP(),credentials.getPort());
	}
	
	private long st;
	private long en;
	
	public long getSt() {
		return st;
	}

	public void setSt(long st) {
		this.st = st;
	}

	public long getEn() {
		return en;
	}

	public void setEn(long en) {
		this.en = en;
	}

	@Override
	public String getURI() {
		int pageSize = Integer.MAX_VALUE;
		return "/harmoney2/tranReceiptCounters/filter?currency=All&type=All&status=C&userId=All&pageSize="
				+pageSize+"&sortColumn=receiptId&sortDirection=desc&startTime="+st+"&endTime="+en;
	}
	
}
