package harmoney.statistics.datacollection;

import harmoney.statistics.datacollection.routines.CounterTransactionsRetrievalRoutine;
import harmoney.statistics.datacollection.routines.CustomerCollectionRoutine;
import harmoney.statistics.datacollection.routines.MoneyBoxLogin;
import harmoney.statistics.model.CounterTransaction;
import harmoney.statistics.model.Customer;
import harmoney.statistics.repository.CounterTransactionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataCollector {

	@Resource
	CounterTransactionRepository cdsRepository;
	
	private boolean dev = true;
        
    private void simulateData(){
    	List<CounterTransaction> list = new ArrayList<CounterTransaction>();
    	for(int i = 0;i<10;i++){
    		CounterTransaction ct  = new CounterTransaction();
    		ct.setAmount(123);
    		ct.setBranchId(1);
    		ct.setCountry("MALAYSIA");
    		ct.setId(i);
    		ct.setTime(System.currentTimeMillis());
    		ct.setType(i%2 == 0 ? "B" : "S");
    		ct.setCustomerName("Rex");
    		list.add(ct);
    	}
    	cdsRepository.save(list);
    }

    
    @Scheduled(cron = "0 0 4 * * ?")
    //@Scheduled (fixedRate=50000)
    public void startCollector(){
    	MoneyBoxLogin mBox = new MoneyBoxLogin("sadmin","");
    	Calendar calendar = new GregorianCalendar();
    	try {
    		String sessionId = mBox.login();
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.add(Calendar.DATE,-1);
			collectCounterTransactions(calendar.getTimeInMillis(),calendar.getTimeInMillis() + 24*60*60*1000-1,sessionId);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    final Logger logger = LoggerFactory.getLogger(DataCollector.class);
    
    public void collectCounterTransactions(long st,long en,String sessionId){
    	logger.info("Start {} End {} ", new Date(st), new Date(en));
    	try {
			CounterTransactionsRetrievalRoutine routine = 
					new CounterTransactionsRetrievalRoutine("sadmin",sessionId);
			routine.setEn(en);
			routine.setSt(st);
			HttpResponse response = routine.execute();
			StringBuffer buffer = routine.getContent(response);
			JSONObject object = new JSONObject(buffer.toString());
			JSONObject data = (JSONObject)object.get("data");
			JSONArray content = (JSONArray)data.get("content");
			logger.info("Collection for {} Data Size {} ",new Date(st) , content.length());
			prepareStatistics(content,collectCustomers(collectAccountIds(content),sessionId),st);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private void prepareStatistics(JSONArray content,Map<Integer,Customer> customerMap,long st){
    	int length = content.length();
    	List<CounterTransaction> list = new ArrayList<CounterTransaction>();
    	for(int i = 0;i<length;i++){
			JSONObject record = (JSONObject)content.get(i);
			int accountId = (Integer) record.get("accountId");
			Customer customer = customerMap.get(accountId);
			String nationality = customer.getCountry();
			CounterTransaction cds = new CounterTransaction();
			String type = (String) record.get("type");
			Double amount = (Double) record.get("amount");
			cds.setAmount(amount);
			cds.setCountry(nationality);
			cds.setType(type);
			cds.setTime((Long)record.get("date"));
			cds.setId((Integer)record.get("id"));
			cds.setBranchId((Integer)record.get("branchId"));
			cds.setCustomerName(customer.getName());
			list.add(cds);
    	}
    	cdsRepository.save(list);
    	logger.info("Saved Successfully");
    }

	private Set<Integer> collectAccountIds(JSONArray content) {
		int length = content.length();
		Set<Integer> accountIds = new HashSet<Integer>();
		for(int i = 0;i<length;i++){
			JSONObject record = (JSONObject)content.get(i);
			accountIds.add((Integer)record.get("accountId"));
		}
		return accountIds;
	}

	private Map<Integer,Customer> collectCustomers(Set<Integer> accountIds,String sessionId) {
		Map<Integer,Customer> customers = new HashMap<Integer,Customer>();
		for(Integer accountId : accountIds){
			CustomerCollectionRoutine routine = new CustomerCollectionRoutine("sadmin",sessionId,accountId);
			try {
				HttpResponse response = routine.execute();
				StringBuffer buffer = routine.getContent(response);
				JSONObject object = new JSONObject(buffer.toString());
				JSONObject data = (JSONObject) object.get("data");
				Customer customer = new Customer();
				if(data != null){
					Object nationality = data.get("nationality");
					if(nationality != null){
						customer.setCountry(nationality.toString());
					}else{
						customer.setCountry("Unknown");
					}
					Object firstName = data.get("firstName");
					if(firstName != null){
						customer.setName(firstName.toString());
					}else{
						customer.setName("Unknown");
					}
					customers.put(accountId, customer);
				}else{
					customer.setName("Unknown");
					customer.setCountry("Unknown");
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return customers;
		
	}
}