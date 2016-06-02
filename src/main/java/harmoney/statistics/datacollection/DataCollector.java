package harmoney.statistics.datacollection;

import harmoney.statistics.datacollection.routines.CounterTransactionsRetrievalRoutine;
import harmoney.statistics.datacollection.routines.CustomerCollectionRoutine;
import harmoney.statistics.datacollection.routines.MoneyBoxLogin;
import harmoney.statistics.model.CounterTransaction;
import harmoney.statistics.model.Credentials;
import harmoney.statistics.model.Customer;
import harmoney.statistics.repository.CounterTransactionRepository;
import harmoney.statistics.repository.CredentialsRepository;

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
	private CounterTransactionRepository cdsRepository;

	@Resource
	private CredentialsRepository credentialsRepository;
	 
    public void takeBackup(){
    	Calendar yesterday = new GregorianCalendar();
    	yesterday.set(Calendar.MILLISECOND, 0);
    	yesterday.set(Calendar.SECOND,0);
    	yesterday.set(Calendar.MINUTE,0);
    	yesterday.set(Calendar.HOUR_OF_DAY,0);
    	yesterday.add(Calendar.DATE,-1);
    
    	Calendar fourYearsBack = (GregorianCalendar)yesterday.clone();
    	fourYearsBack.add(Calendar.YEAR,-4);
    	
    	Credentials credentials = getCredentials();
    	MoneyBoxLogin mBox = new MoneyBoxLogin(credentials.getUserName(),credentials.getPassword(),credentials.getPort());
		try {
			String sessionId = mBox.login();
			runBackUp(fourYearsBack,yesterday,sessionId,credentials.getPort());
		} catch (ClientProtocolException e) {
			logger.error("",e);

		} catch (IOException e) {
			logger.error("",e);
		}
		
    }
    
    private void runBackUp(Calendar fourYearsBack, Calendar yesterday,String sessionId,int port){
    	while(fourYearsBack.before(yesterday)){
    		collectCounterTransactions(fourYearsBack.getTimeInMillis(),fourYearsBack.getTimeInMillis() + 24*60*60*1000-1,sessionId,port);
    		fourYearsBack.add(Calendar.DATE, 1);
    	}
    }
    
    @Scheduled(cron = "0 0 4 * * ?")
    public void startDailyCollector(){
    	Credentials credentials = getCredentials();
    	MoneyBoxLogin mBox = new MoneyBoxLogin(credentials.getUserName(),credentials.getPassword(),credentials.getPort());
    	Calendar calendar = new GregorianCalendar();
    	try {
    		String sessionId = mBox.login();
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND,0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.HOUR_OF_DAY,0);
			calendar.add(Calendar.DATE,-1);
			collectCounterTransactions(calendar.getTimeInMillis(),calendar.getTimeInMillis() + 24*60*60*1000-1,sessionId,credentials.getPort());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    final Logger logger = LoggerFactory.getLogger(DataCollector.class);
    
    public void collectCounterTransactions(long st,long en,String sessionId,int port){
    	logger.info("Start {} End {} ", new Date(st), new Date(en));
    	
    	try {
			CounterTransactionsRetrievalRoutine routine = 
					new CounterTransactionsRetrievalRoutine("sadmin",sessionId,port);
			routine.setEn(en);
			routine.setSt(st);
			HttpResponse response = routine.execute();
			StringBuffer buffer = routine.getContent(response);
			JSONObject object = new JSONObject(buffer.toString());
			JSONObject data = (JSONObject)object.get("data");
			JSONArray content = (JSONArray)data.get("content");
			logger.info("Collection for {} Data Size {} ",new Date(st) , content.length());
			prepareStatistics(content,collectCustomers(collectAccountIds(content),sessionId,port),st);
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

	private Map<Integer,Customer> collectCustomers(Set<Integer> accountIds,String sessionId,int port) {
		Map<Integer,Customer> customers = new HashMap<Integer,Customer>();
		for(Integer accountId : accountIds){
			CustomerCollectionRoutine routine = new CustomerCollectionRoutine("sadmin",sessionId,accountId,port);
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

	public void checkAndCollectForToday(long from, long to) {
		GregorianCalendar today = new GregorianCalendar();
		today.set(Calendar.MILLISECOND, 0);
		today.set(Calendar.SECOND,0);
		today.set(Calendar.MINUTE,0);
		today.set(Calendar.HOUR_OF_DAY,0);
		
		long todayStart =  today.getTimeInMillis();
		long todayEnd = todayStart + (24*60*60*1000) - 1;
		
		if(from > todayStart && from < todayEnd){
			collectForToday(todayStart,todayEnd);
		}
		
		if(to > todayStart && to < todayEnd){
			collectForToday(todayStart,todayEnd);
		}
	}

	private void collectForToday(long from,long to) {
		logger.info("Going to collect for today...");
		Credentials credentials = getCredentials();
    	MoneyBoxLogin mBox = new MoneyBoxLogin(credentials.getUserName(),credentials.getPassword(),credentials.getPort());
		try {
			String sessionId = mBox.login();
			collectCounterTransactions(from,to,sessionId,credentials.getPort());
		} catch (ClientProtocolException e) {
			logger.error("",e);

		} catch (IOException e) {
			logger.error("",e);
		}
	}
	
	private Credentials getCredentials(){
		List<Credentials> list = credentialsRepository.findAll();
		if(list.size() == 0){
			logger.error("No Credentials are provided... we cannot do anything");
			return null;
		}
		return list.get(0);
	}
	
	
}