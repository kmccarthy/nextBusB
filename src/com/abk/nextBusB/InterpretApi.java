package com.abk.nextBusB;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

//import android.text.Html;
//import android.util.Log;

public final class InterpretApi {
	
	private InterpretApi() {
	}
	
	public static String interpretOneTrip(JSONObject trip) {
		String inMinutes = "";
		double adjustmentAge;
		String adjustedScheduletime;
		
		try {
			adjustedScheduletime = trip.getString("AdjustedScheduleTime");
	        Double tmp = Double.valueOf(trip.getString("AdjustmentAge"));
	        adjustmentAge = tmp.doubleValue();
		} catch (Exception e) {
			//Log.e("Exception", e.getMessage());
			HTTPDemo.errorDialog("interpretOneTrip threw " + e.toString());
			return "";
		} finally {
		}
		
        if (adjustmentAge == -1.0) {
        	inMinutes += "<strong><font color='#0000FF'>" + adjustedScheduletime + "</font></strong>&nbsp;";
        } else if (adjustmentAge > 2.0) {
        	inMinutes += "<strong><font color='#FF0000'>" + adjustedScheduletime + "</font></strong>&nbsp;";
        } else {
        	inMinutes += "<strong><font color='#000000'>" + adjustedScheduletime + "</font></strong>&nbsp;";
        }
		return inMinutes;
	}
	
	public static String interpretOneRoute(JSONObject route) {
		String displayRoute = "";

		JSONArray trips = new JSONArray();
		
		try {
			if (route.isNull("Trips") == false) {
				Object tmp = route.get("Trips");
				if (tmp instanceof JSONObject) {
					trips.put(route.getJSONObject("Trips"));
				}
				else if (tmp instanceof JSONArray) {
					trips = route.getJSONArray("Trips");
				}
			}
	
			String routeNo = route.isNull("RouteNo") ? "No Route" : route.getString("RouteNo");
			String routeHeading = route.isNull("RouteHeading") ? "" : route.getString("RouteHeading");
			
			String inMinutes = "";
			if (trips.length() == 0) {
				inMinutes = "no scheduled times available&nbsp;";
			}
			
			
			// trips is always a JSONArray
			for (int j=0; j < trips.length(); j++) {
				JSONObject row2 = trips.getJSONObject(j);
	    		
	    		if (row2.isNull("Trip")) {
	    			// the case where "Trips' is an array of objects
		    		inMinutes += InterpretApi.interpretOneTrip(row2);
		    		
	    		} else {
	    			// the case where "Trips" is an object with a list of "Trip" objects
	    			JSONArray trip3 = row2.getJSONArray("Trip");
	    			for (int k=0; k < trip3.length(); k++ ) {
						JSONObject row3 = trip3.getJSONObject(k);
			    		inMinutes += InterpretApi.interpretOneTrip(row3);
					}
	    		}
			}
			
			displayRoute = "#" + routeNo + "&nbsp;(" + routeHeading + ")<br>in&nbsp;" + inMinutes;
			displayRoute += "<font color='#FF0C090A'>(minutes)</font>";
			displayRoute += "<br><br>";
		
		} catch (Exception e) {
			//Log.e("Exception", e.getMessage());
			HTTPDemo.errorDialog("interpretOneRoute threw " + e.toString());
			return "";
		} finally {
		}
		
		return displayRoute;
	}

	public static String interpretRouteSummaryForStopResult(JSONObject summary) {
		String display = "";
		try {
			if (summary.isNull("Routes")) {
	    		return "No Data2";
	    	}
	    	
	    	Object t1 = summary.get("Routes");
	    	if (t1 instanceof JSONArray) {
	    		return  "unable to parse Routes";
	    	}
	    	
	    	JSONObject routes = summary.getJSONObject("Routes");
	    	
	    	if (routes.isNull("Route")) {
	    		return "No Data3";
	    	}
	    	
	    	Object t2 = routes.get("Route");
	    	JSONArray route = new JSONArray();
	    	if (t2 instanceof JSONObject) {
	    		route.put(routes.getJSONObject("Route"));
	    	}
	    	else if (t2 instanceof JSONArray) {
	    		route = routes.getJSONArray("Route");
	    	}
	   
	    	for (int i = 0; i < route.length(); i++) {
	    		JSONObject row = route.getJSONObject(i);
	    		display += InterpretApi.interpretOneRoute(row);
	    	}
	    	
	    	if (display.length() == 0) {
	    		display = "no information available for this stop";
	    	}
    	
	    } catch (Exception e) {
			//Log.e("Exception", e.getMessage());
			HTTPDemo.errorDialog("interpretRouteSummaryForStopResult threw " + e.toString());
			return "No Data4";
		} finally {
		}
	
		return display;
	}
}
