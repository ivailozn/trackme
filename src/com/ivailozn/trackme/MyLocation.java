package com.ivailozn.trackme;

import java.io.Serializable;

import android.location.Location;

/**
 * Serialazible version of the android Location class
 * 
 * @author ivaylo
 * 
 */
public class MyLocation implements Serializable {

	double latitude, longitude;
	long time;
	float bearing, accuracy, speed;
	String provider;

	public MyLocation() {
	}

	MyLocation(Location location) {
		bearing = location.getBearing();
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		time = location.getTime();
		accuracy = location.getAccuracy();
		speed = location.getSpeed();
		provider = location.getProvider();
	}

	Location getLocatoin() {

		Location l = new Location("");
		l.setBearing(bearing);
		l.setAccuracy(accuracy);
		l.setSpeed(speed);
		l.setTime(time);
		l.setProvider(provider);
		l.setLatitude(latitude);
		l.setLongitude(longitude);

		return l;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public float getBearing() {
		return bearing;
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

}
