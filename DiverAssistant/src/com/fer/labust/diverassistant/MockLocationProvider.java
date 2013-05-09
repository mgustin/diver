package com.fer.labust.diverassistant;

import java.io.IOException;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.util.Log;

public class MockLocationProvider extends Thread {


private LocationManager locationManager;

private String mocLocationProvider;

private String LOG_TAG = "MOCK: ";

public boolean run_flag = true;

public boolean new_data_flag = false;

private double i = 0;

public MockLocationProvider(LocationManager locationManager, String mocLocationProvider) throws IOException {

    this.locationManager = locationManager;
    this.mocLocationProvider = mocLocationProvider;
}

@Override
public void run() {

    while(true) {

    	if(!run_flag)
    		break;
        try {

            Thread.sleep(1000);

        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        i += 0.0000001;
        if(new_data_flag){
        	// Set one position
        	Double latitude = 40.0 + i;
        	Double longitude = 20.0 + i;
        	Double altitude = 0.0;
        	Location location = new Location(mocLocationProvider);
        	location.setLatitude(latitude);
        	location.setLongitude(longitude);
        	location.setAltitude(altitude);
        	location.setProvider(mocLocationProvider);


        	// set the time in the location. If the time on this location
        	// matches the time on the one in the previous set call, it will be
        	// ignored
        	location.setTime(System.currentTimeMillis());
        	Log.e(LOG_TAG, location.toString());

        	locationManager.setTestProviderStatus(mocLocationProvider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
        	locationManager.setTestProviderLocation(mocLocationProvider, location);
        	new_data_flag = false;
        }
    }
}
}