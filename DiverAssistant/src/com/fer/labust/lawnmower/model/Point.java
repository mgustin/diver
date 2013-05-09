package com.fer.labust.lawnmower.model;

public class Point {
	private double longitude;
	private double latitude;

	public Point() {
		this.latitude = 0;
		this.longitude = 0;
	}

	public Point(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public boolean isBetween(Point a, Point b) {
		double lowerLatitude = a.latitude;
		double higherLatitude = b.latitude;
		if (b.latitude < a.latitude) {
			lowerLatitude = b.latitude;
			higherLatitude = a.latitude;
		}
		double lowerLongitude = a.longitude;
		double higherLongitude = b.longitude;
		if (b.longitude < a.longitude) {
			lowerLongitude = b.longitude;
			higherLongitude = a.longitude;
		}
		if ((this.longitude > higherLongitude)
				|| (this.longitude < lowerLongitude)
				|| (this.latitude > higherLatitude)
				|| (this.latitude < lowerLatitude)) {
			return false;
		} else {
			return true;
		}
	}

	public String toString() {
		return "(" + this.longitude + "," + this.latitude + ")";
	}

	public double getDistanceTo(Point point) {
		double lat1 = this.latitude;
		double lng1 = this.longitude;
		double lat2 = point.getLatitude();
		double lng2 = point.getLongitude();

		double pi80 = Math.PI / 180;
		lat1 *= pi80;
		lat2 *= pi80;
		lng1 *= pi80;
		lng2 *= pi80;

		double r = 6372.795477598; // mean radius of Earth in km
		double dLat = lat2 - lat1;
		double dLng = lng2 - lng1;
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1)
				* Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return r * c * 1000; // rezultat u metrima
	}
}
