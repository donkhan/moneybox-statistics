package harmoney.statistics.web;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import harmoney.statistics.datacollection.DataCollector;
import harmoney.statistics.model.CounterTransaction;
import harmoney.statistics.model.CountryStatistics;
import harmoney.statistics.model.CountryStatisticsCollection;
import harmoney.statistics.model.Credentials;
import harmoney.statistics.model.SessionMap;
import harmoney.statistics.repository.CounterTransactionRepository;
import harmoney.statistics.repository.CredentialsRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.bson.BSONObject;
import org.bson.types.BasicBSONList;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DBObject;

@RestController
public class StatisticsController {

	final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Resource
	private CredentialsRepository credentialsRepository;

    @Resource
	private CounterTransactionRepository cdsRepository;
    
	@RequestMapping("/")
    @CrossOrigin
    public Response index() {
		logger.info("Credentials Repo {}",credentialsRepository);
    	String result = "This Micro Service will provide statistics data of Money Box";
        return Response.ok().entity(result).header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
    }
	
	@RequestMapping(value = "/update-credentials", method = RequestMethod.POST, headers = "Accept=application/json", 
    		produces = "application/json")
	public Response updateCredentials(@RequestBody final Credentials credentials,HttpServletRequest request) {
		if(!isAuthenticatedRequest(request)){
    		return Response.serverError().build();
    	}
		List<Credentials> list = credentialsRepository.findAll();
		credentialsRepository.deleteAll();
		credentialsRepository.save(credentials);
		
		return Response.ok().header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Methods", "POST").build();
	}
    
	@RequestMapping(value = "/tran-statistics/take-backup", method = RequestMethod.GET, headers = "Accept=application/json", 
    		produces = "application/json")
	public Response takeBackup(HttpSession session,HttpServletRequest request, HttpServletResponse response) {
		if(!isAuthenticatedRequest(request)){
    		return Response.serverError().build();
    	}
		DataCollector dc = new DataCollector();
		dc.takeBackup(credentialsRepository,cdsRepository);
		return Response.ok().header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Methods", "GET").build();
	}
	
    @RequestMapping(value = "/tran-statistics/counter/download-report", method = RequestMethod.GET, headers = "Accept=application/json", 
    		produces = "application/json")
	public Response downloadReport(HttpSession session,HttpServletRequest request, HttpServletResponse response) {
    	if(!isAuthenticatedRequest(request)){
    		return Response.serverError().build();
    	}
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			InputStream inputStream = classLoader.getResourceAsStream("branchstatistics.jrxml");
			DateFormat format = DateFormat.getDateInstance();
			byte output[] = prepareJasperReport(inputStream,getData(request),
					format.format(new Date(Long.parseLong(request.getParameter("start-time")))),
					format.format(new Date(Long.parseLong(request.getParameter("end-time")))),
					request.getParameter("branchId"));
			response.getOutputStream().write(output);
			response.getOutputStream().close();
			CountryStatisticsCollection csc = getData(request);
			JSONObject user = getUser(request);
	    	audit(user,"Downloaded Counter Transaction Statistics Report","SUCCESS");
			return Response.ok().entity(csc).header("Access-Control-Allow-Origin", "*")
	    			.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return Response.ok().build();
	}
    
    @RequestMapping("/tran-statistics/counter")
    @CrossOrigin
    public Response counterStatistics(HttpServletRequest request) {
    	if(!isAuthenticatedRequest(request)){
    		return Response.serverError().build();
    	}
    	
    	JSONObject user = getUser(request);
    	audit(user,"Retrieved Counter Transaction Statistics","SUCCESS");
    	CountryStatisticsCollection csc = getData(request);
    	return Response.ok().entity(csc).header("Access-Control-Allow-Origin", "*")
    			.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT").build();
    }


    private boolean isAuthenticatedRequest(HttpServletRequest request){
    	String  token = request.getParameter("token");
    	SessionMap sessionMap = SessionMap.getSessionMap();
    	if(!sessionMap.containsKey(token)){
    		logger.error("Unable to serve as token {} is not present in Statistics Server ",token);
    		return false;
    	}
    	return true;
    }
    
    private JSONObject getUser(HttpServletRequest request){
    	String  token = request.getParameter("token");
    	SessionMap sessionMap = SessionMap.getSessionMap();
    	JSONObject user = sessionMap.get(token);
    	return user;
    }
    
    private byte[] prepareJasperReport(InputStream inputStream,CountryStatisticsCollection dataSource,String fromDateS,String toDateS,String branchId){
		try {
			JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("period", fromDateS + " to " + toDateS);
			if(branchId.equals("-1")){
				params.put("branch", "ALL");
			}else{
				//params.put("branch", branchService.findById(Long.parseLong(branchId)).getName());
				params.put("branch", "Yet to be determined");
			}
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params,dataSource);
			byte output[] = JasperExportManager.exportReportToPdf(jasperPrint);
			return output;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
    
    
    
    private CountryStatisticsCollection getData(HttpServletRequest request){
    	long from = Long.parseLong(request.getParameter("start-time"));
    	long to = Long.parseLong(request.getParameter("end-time"));
    	logger.info("From {} " , new Date(from));
    	logger.info("To {}" , new Date(to));
    	
    	DataCollector dataCollector = new DataCollector();
    	dataCollector.checkAndCollectForToday(from,to,credentialsRepository,cdsRepository);
    	
    	Criteria criteria = Criteria.where("time").gte(from).lte(to);
    	if(request.getParameter("branchId") != null){
    		long branchId = Long.parseLong(request.getParameter("branchId"));
    		if(branchId != -1){
    			Criteria branchCriteria = Criteria.where("branchId").is(branchId);
    			criteria.andOperator(branchCriteria);
    		}
    	}
    	
    	TypedAggregation<CounterTransaction> aggregation = newAggregation(CounterTransaction.class,
    			match(criteria),
    		    group("country","type").
    		    sum("amount").as("totalAmount").
    		    count().as("noOfTransactions")
		);

    	logger.info("{}",aggregation.toString());
    	AggregationResults<CountryStatistics> aresult = mongoTemplate.aggregate(aggregation, CountryStatistics.class);
    	DBObject db = aresult.getRawResults();
    	BSONObject bo = (BSONObject)db.get("result");
   
    	CountryStatisticsCollection csc = new CountryStatisticsCollection();
    	convert(csc,bo);
    	logger.info("No of Records {}",csc.getItems().size());
    	return csc;
    }


	private void convert(CountryStatisticsCollection csc, BSONObject bo) {
		Map<String,CountryStatistics> map = new HashMap<String,CountryStatistics>();
		
		if(bo instanceof BasicBSONList){
			BasicBSONList  bbl = (BasicBSONList) bo;
			int size = bbl.size();
			for(int i = 0;i<size;i++){
				BSONObject b = (BSONObject)bbl.get(i);
				BSONObject id = (BSONObject) b.get("_id");
				String country = (String)id.get("country");
				CountryStatistics cs;
				if(!map.containsKey(country)){
					cs = new CountryStatistics();
					cs.setCountry(country);
					map.put(country, cs);
				}else{
					cs = map.get(country);
				}
				String type = (String)id.get("type");
				if(type.equals("B")){
					cs.setTotalBuyValue((Double)b.get("totalAmount"));
					cs.setTotalBuy(1.0 * (Integer)b.get("noOfTransactions"));
				}else{
					cs.setTotalSellValue((Double)b.get("totalAmount"));
					cs.setTotalSell(1.0 * (Integer)b.get("noOfTransactions"));
				}
			}
		}
		
		Iterator<CountryStatistics> iterator = map.values().iterator();
		double tb = 0, ts = 0;
		double tbv = 0L,tsv = 0L;
		while(iterator.hasNext()){
			CountryStatistics cs = iterator.next(); 
			csc.addItem(cs);
			tb += cs.getTotalBuy(); ts = cs.getTotalSell();
			tbv += cs.getTotalBuyValue(); tsv += cs.getTotalSellValue();
		}
		List<CountryStatistics> items = csc.getItems();
		for(CountryStatistics cs : items ){
			if(tb != 0)	cs.setBuyPercentage((double)cs.getTotalBuy()*100/tb);
			if(ts != 0) cs.setSellPercentage((double)cs.getTotalSell()*100/ts);
			if(tbv != 0) cs.setBuyValuePercentage((double)cs.getTotalBuyValue()*100/tbv);
			if(tsv != 0) cs.setSellValuePercentage((double)cs.getTotalSellValue()*100/tsv);
		}
	}
	
	public void sendAuditLog(String syslogMessage){	
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = syslogMessage.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5678);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (SocketException e) {
			logger.error("Socket Exception {}",e);
		} catch(UnknownHostException ukhe){
			logger.error("Unknown Host Exception {}",ukhe);
		}catch(IOException ioe){
			logger.error("IO Exception ",ioe);
		}
		logger.info("SysLog Sent");
	}
	
	 private void audit(JSONObject user, String message,String status) {
		 JSONObject x = new JSONObject();
		 x.put("time",System.currentTimeMillis());
		 x.put("branch", user.get("branch"));
		 x.put("message", message);
		 x.put("module","Statistics");
		 x.put("user", user.get("id"));
		 x.put("status",status);
		 x.put("messageType", "LOG");
		 sendAuditLog(x.toJSONString());
	}
}