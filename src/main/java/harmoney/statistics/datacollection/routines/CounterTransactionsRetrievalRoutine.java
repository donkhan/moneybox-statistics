package harmoney.statistics.datacollection.routines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;


public class CounterTransactionsRetrievalRoutine extends MoneyBoxGet{
	
	public CounterTransactionsRetrievalRoutine(String userName,String sessionId,int port){
		super(userName,sessionId,port);
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
		
	public StringBuffer getContent(HttpResponse response) throws IOException{
		InputStream is = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String s = "";
		while((s = reader.readLine()) != null){
			buffer.append(s);
		}
		reader.close();
		is.close();
		return buffer;
	}
	

}
