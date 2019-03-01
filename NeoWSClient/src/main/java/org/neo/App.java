package org.neo;

/**
 * Hello world!
 *
 */
public class App 
{

	public static boolean DEBUG = true;

	/*
	 * Main
	 * 
	 * This program extracts “Near Earth Objects” using the NASA RESTful Web Service https://api.nasa.gov/api.html#NeoWS 
	 * Two separate criteria are extracted:
	 * 		1) Identifies which NEO is the largest in size 
	 * 		2) which NEO is the closest to Earth within the date range. 
     *
 	 *  Output the total number of NEOs, and the details retrieved for both the largest and closest NEOs identified.
     *
	 * 
	 * 	Note: startDate, endDate and numberOfIntervals could be passed as command-line args (not implemented)
	 *  
	 * Note that using endDate is really just providing a restrictive date but also the size of the intervals 
	 * In case you want to look at longer date ranges is best to just leave endDate as default (null) which defaults to 7 days.
	 * Each response provides a link to the next interval, so the number of "next" links to follow
	 * can be specified in the numberOfIntervals.
	 * So the range looked at is date rate X number of intervals.
	 * So a 7 day interval range for 100 intervals will give you 700 days.
	 * 
	 * Also note: NASA's NEO REST APi will only allow 1K REST calls per hour, each numberOfIntervals is a REST call
	 *     So this significantly limits how much data can be obtained in one run.
	 * 
	 *  The default startDate below is today's sate since we want to see the future NEO risks.
	 */
	public static void main(String[] args) {
		DEBUG = false; // for more verbose debugging output
		int numberOfIntervals = 2; // this is how many "next" intervals to follow
		String startDate = Utils.getNowDateFormatted();
		String endDate = Utils.getNowPlusNDateFormatted(5);
		if(DEBUG)
			System.out.println("NEO data from "+ startDate + " to " + endDate);
		NeoWSClient n = new NeoWSClient();
		try {
			String uri = n.buildURI(startDate, endDate);
			n.connectAndProcess(uri,numberOfIntervals);
			n.outputSummary(startDate, endDate, numberOfIntervals);
		} catch (Exception e) {
			System.out.println("ERROR: failed to return NEO data");
			e.printStackTrace();
		}
	}

}

