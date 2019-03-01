package org.neo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.*;


/*
 * Coding test:

• Write a Java application to get a list of “Near Earth Objects” using the NASA RESTful Web Service 
https://api.nasa.gov/api.html#NeoWS 
• Identify which NEO is the largest in size and which is the closest to Earth – this requires your judgment to define . 

Output the total number of NEOs, and the details retrieved for both the largest and closest NEOs identified.
• Provide proof that code is functional. 
It must be possible to download, build and run the application on any platform where Java is installed.


 * 
 */




/*
 * class NeoWSClient 
 * 	
 * 
 */
public class NeoWSClient {

	/* NASA_API_KEY
	 You can apply for a key here: https://api.nasa.gov/index.html#apply-for-an-api-key
	 Key have limited usage
 	Limits are placed on the number of API requests you may make using your API key. 
 	Rate limits may vary by service, but the defaults are:
	Hourly Limit: 1,000 requests per hour
	For each API key, these limits are applied across all api.nasa.gov API requests. 
	Exceeding these limits will lead to your API key being temporarily blocked from making 
	further requests. The block will automatically be lifted by waiting an hour. 
	 */
	private static final String CARLOS_NASA_API_KEY = "hqkBSsYW6Bb2OR7jXwcZnxa6MuBhtU3r8he8fiFG";
	private static final String NASA_API_KEY = CARLOS_NASA_API_KEY;	
	private static final String NASA_NEO_URI = "https://api.nasa.gov/neo/rest/v1/feed?start_date=START_DATE&api_key=" + NASA_API_KEY;

	private static final String START_DATE_STR = "START_DATE";
	private static final String END_DATE_URI_STR = "&end_date=";

	// Using Hashmap instead of List in order to handle duplicates
	private static HashMap<String,Neo> NeoSizeAndDist = new HashMap<>();


	/*
	 * Allows to accept any SSL Certificate, poor security, good for "code tests"
	 */
	private static class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}


	/*
	 * Method: buildURI
	 * 
	 * description: builds NASA NEO URI
	 * can include or exclude end-Date, just set as null to exclude it, default is 7 days for no endDate

	   start_date	YYYY-MM-DD	none	Starting date for asteroid search
	   end_date	YYYY-MM-DD	7 days after start_date	Ending date for asteroid search
  	 */
	public String buildURI(String startDate,String endDate) throws Exception {
		if(startDate == null || startDate.trim().isEmpty())
			throw new Exception("Error: Invalid start date. Start date given:" + startDate);
		String uri = NASA_NEO_URI;
		if(endDate!=null && !endDate.trim().isEmpty()) {
			// if given, add the end_date param
			uri = uri.replace(START_DATE_STR, startDate + END_DATE_URI_STR + endDate);
		}else {
			uri = uri.replace("START_DATE", startDate);
		}
		return uri;    	
	}

	/*
	 * Method: connectAndProcess
	 * 
	 */
	public void connectAndProcess(String uri, int iterateCount) throws Exception {

		// configure the SSLContext with a TrustManager
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
		SSLContext.setDefault(ctx);

		HttpsURLConnection conn = null;
		String nextUri=uri;
		for(int iterUri=0;iterUri<iterateCount;iterUri++)
		{
			try {

				URL url = new URL(nextUri);
				System.out.println("Processing URI ("+iterUri+"): " + nextUri);
				conn = (HttpsURLConnection) url.openConnection();

				conn.setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}
				});

				if (conn.getResponseCode() != 200) {
					throw new RuntimeException("Error: HTTP error code : " + conn.getResponseCode());
				}


				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));

				String allOutput = "";
				String output;
				if(App.DEBUG)
					System.out.println("Response from NASA NeoWS=>");
				while ((output = br.readLine()) != null) {
					if (App.DEBUG)
						System.out.println(output);
					allOutput += output;
				}

				// Process the Neo data
				parseNeoData(allOutput);

				// this below is good to have but with more than 1 interval is hard to tell if duplicates are counted more than once
				// the Neo data contains an element_count field
				//int expectedCount = parseNeoDataElementCount(allOutput);
				//int countNeos = NeoSizeAndDist.size();
				//if(App.DEBUG)
				//	System.out.println("Neo count:" + countNeos + "  And count according to element_count field: " + expectedCount);

				// Get Next URI
				nextUri = parseNextUri(allOutput);
			} catch (MalformedURLException e) {
				System.out.println("MalformedURL error: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO error: " + e.getMessage());
			}
		
			conn.disconnect();
		}
	}
	
	
	/*
	 * Method: outputSummary 
	 * 
	 * Description: generates the Summary Report
	 * 
	 */
	protected void outputSummary(String startDate, String endDate, int numberOfIntervals) {
		// find least distance an largest size
		String largestNeo_Id = "";
		double findLargestNeo = 0;
		String shortestDistance_Id = "";
		double findShortestDistance = Double.MAX_VALUE;
		for(Entry<String, Neo> entry : NeoSizeAndDist.entrySet()) {
			if(entry!=null) {	
				 
				double size = entry.getValue().getSize();
				if(size>findLargestNeo) {
					findLargestNeo = entry.getValue().getSize();
					largestNeo_Id = entry.getKey();
				}

				double dist = entry.getValue().getDistance();
				if(dist<findShortestDistance) {
					findShortestDistance = dist;
					shortestDistance_Id = entry.getKey();
				}			
			}
			
		}

		System.out.println();
		System.out.println("=======================================================================");
		System.out.println("Data processed from startDate " + startDate + " EndDate " + endDate + "  Number of Intervals " + numberOfIntervals);
		System.out.println("=======================================================================");

		int totalNeos = NeoSizeAndDist.size();
		System.out.println();
		System.out.println("=======================================================================");
		System.out.println("Total Number of Neos in given date range: " + totalNeos);
		System.out.println("=======================================================================");
		System.out.println();
		System.out.println("=======================================================================");
		System.out.println("The largest Neo in given date range: Id: " + largestNeo_Id + " diameter (feet):" + NeoSizeAndDist.get(largestNeo_Id).getSize());
		System.out.println("Neo Details=> " + NeoSizeAndDist.get(largestNeo_Id).getJsonObj().toString());
		System.out.println("=======================================================================");
		System.out.println();
		System.out.println("=======================================================================");
		System.out.println("The Closest to Earth Neo in given date range:  Id: " + shortestDistance_Id  + " distance (KMs):" + NeoSizeAndDist.get(shortestDistance_Id).getDistance());
		System.out.println("Close approach date: " + NeoSizeAndDist.get(shortestDistance_Id).getApproachDate());
		System.out.println("Neo Details=> " + NeoSizeAndDist.get(shortestDistance_Id).getJsonObj().toString());
		System.out.println("=======================================================================");

	}

	
	/*
	 * method: parseNextUri
	 * 
	 * Description: extracts the "next" interval uri
	 * 
	 */
	protected String parseNextUri(String data) throws JSONException {
		JSONObject obj = new JSONObject(data);
		JSONObject links = obj.getJSONObject("links");
		String next = (String) links.get("next");
		return next;
	}

	/*
	private void prettyPrintJson(String s) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		String prettyJsonString = gson.toJson(je);

	}
	 */


	/*
	 * parseNeoData
	 * 	-extracts data
	 */
	protected void parseNeoData(String data) throws JSONException, ParseException {
		JSONObject obj = new JSONObject(data);
		JSONObject neo = obj.getJSONObject("near_earth_objects");
		Iterator<String> keys = neo.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			Object item = neo.get(key);
			if (item instanceof JSONArray) {
				JSONArray arr = (JSONArray) item;
				for(int i=0;i<arr.length();i++) {
					extractNeoObject(arr.get(i));
				}
			}
		}
	}


	/*
	 * parseNeoDataElementCount
	 * 	-extracts Element count from json data
	 *  -it is unknown if the element count given are unique Neos (rather than number of entries),
	 *     so not trustful to be used at this time
	 */
	protected int parseNeoDataElementCount(String data) throws Exception {
		JSONObject obj = new JSONObject(data);

		Object ec = obj.get("element_count");
		int val = (int) ec;
		return val;
	}


	/*
	 *  Method: extractNeoObject
	 * 
	 * 
	 */
	protected void extractNeoObject(Object obj) throws JSONException, ParseException {
		if (obj instanceof JSONObject) {
			Object id = ((JSONObject) obj).get("id");

			// diameter
			Object ed = ((JSONObject) obj).get("estimated_diameter");
			Object f = ((JSONObject) ed).get("feet");
			Object fmax = ((JSONObject) f).get("estimated_diameter_max");
			Object fmin = ((JSONObject) f).get("estimated_diameter_min");
			Double fmean = ((Double)fmax + (Double)fmin)/2;
			if(App.DEBUG)
				System.out.println("Neo id "+ id + " Diameter (feet) max " + fmax + " min " + fmin + " Use mean " + fmean);
			setNeoSizeEntry((String)id,fmean,(JSONObject) obj);

			// approach kms
			Object cad = ((JSONObject) obj).get("close_approach_data");
			JSONArray cadArr = (JSONArray)cad;
			// Is an array of approaches so just find the closest one
			double closestValue=Double.MAX_VALUE;
			Object approachDate=null;
			for(int i=0;i<cadArr.length();i++) {
				JSONObject o = (JSONObject) cadArr.get(i);
				approachDate = o.get("close_approach_date");
				Object md = o.get("miss_distance");
				Object kms = ((JSONObject) md).get("kilometers");
				double kms_d = Double.parseDouble((String) kms);
				if(closestValue>kms_d)
					closestValue = kms_d;
			}

			if(App.DEBUG)
				System.out.println("Neo id "+ id + " Miss distance (kms) " + closestValue);
			setNeoDistEntry((String)id, closestValue, (String)approachDate, (JSONObject) obj);
		}
	}

	/*
	 * Method: setNeoSizeEntry
	 * 
	 * Description: assigns "size" to a hashmap Neo object entry
	 */
	protected void setNeoSizeEntry(String id, Double size, JSONObject jsonObj) throws ParseException {
		Neo o = NeoSizeAndDist.get(id);
		if(o==null) { 
			o = new Neo(id,size,Double.MAX_VALUE,null,jsonObj);
			NeoSizeAndDist.put(id, o);
		} else {
			o.setSize(size);
		}
	}

	/*
	 * Method: setNeoDistEntry
	 * 
	 * Description: assigns "dist" to a hashmap Neo object entry
	 */
	protected void setNeoDistEntry(String id, Double dist, String approachDate, JSONObject jsonObj) throws ParseException {
		Neo o = NeoSizeAndDist.get(id);
		if(o==null) { 
			o = new Neo(id,Double.MIN_VALUE,dist, approachDate, jsonObj);
			NeoSizeAndDist.put(id, o);
		} else {
			o.setDist(dist,approachDate);
		}
	}
}
