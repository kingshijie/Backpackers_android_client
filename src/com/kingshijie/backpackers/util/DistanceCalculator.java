package com.kingshijie.backpackers.util;

public class DistanceCalculator {
	private final static double EARTH_RADIUS = 6371.004;
	private final static double EARTH_PERIMETER = 40075.04;
	
	private static double rad(double d){
		return d * Math.PI / 180;
	}
	
	public static double GetFineDistance(double lati1, double long1, double lati2, double long2)
	{
	    double dLat = rad(lati2-lati1);
	    double dLon = rad(long2-long1);
	    double lat1 = rad(lati1);
	    double lat2 = rad(lati2);

	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	            Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	    double d = EARTH_RADIUS * c;
	    return d;
	}
	
	public static double GetCoarseDistance(double lati1, double long1, double lati2, double long2){
		double lat1 = rad(lati1);
		double lat2 = rad(lati2);
		double lon1 = rad(long1);
		double lon2 = rad(long2);
		double x = (lon2-lon1) * Math.cos((lat1+lat2)/2);
		double y = (lat2-lat1);
		double d = Math.sqrt(x*x + y*y) * EARTH_RADIUS;
		return d;
	}
	
	public static double getRange(int dis){
		double m = EARTH_PERIMETER/360;
		return ((double)dis)/m;
	}
	
	public static double getRange(double dis){
		double m = EARTH_PERIMETER/360;
		return dis/m;
	}
}
