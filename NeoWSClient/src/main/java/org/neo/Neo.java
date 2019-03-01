package org.neo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

/*
 * class Neo
 * 
 * Description: an object to store an Neo
 */
public class Neo {
	String id;
	double size; // diameter size in feet, not sure why I chose feet, perhaps meter is better...
	double distance; // distance to earth in KMs
	Date approachDate; // date the Neo wil be closest to earth
	JSONObject jsonObj; // not yet using the rest of the fields so just keep them in a JSONObject

	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public Neo(String id, double size, double dist, String approachDate, JSONObject jsonObj) throws ParseException {
		this.jsonObj = jsonObj;
		this.size = size;
		this.distance = dist;
		if(approachDate!=null && !approachDate.trim().isEmpty())
			this.approachDate = dateFormat.parse(approachDate); // throws in case date is not parseable
	}

	public String getId() { return id; }
	public double getSize() { return size; }
	public double getDistance() { return distance; }
	public String getApproachDate() {
		if(this.approachDate!=null)
			return this.approachDate.toString();
		else return "empty";
	}
	public JSONObject getJsonObj() { return jsonObj; }

	public void setId(String id) { this.id = id; }
	public void setSize(double size) { this.size = size; }
	public void setDist(double dist, String approachDate) throws ParseException { 
		this.distance = dist; 
		this.setApproachDate(approachDate); 
	}
	private void setApproachDate(String approachDate) throws ParseException {
		if(approachDate!=null && !approachDate.trim().isEmpty())
			this.approachDate = dateFormat.parse(approachDate); // throws in case date is not parseable
	}
	public void setJsonObj(JSONObject o) { this.jsonObj = o; }


}

