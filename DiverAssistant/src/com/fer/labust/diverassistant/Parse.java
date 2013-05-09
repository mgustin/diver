package com.fer.labust.diverassistant;

import java.io.IOException;

import com.fer.labust.diverassistant.MainActivity.CommunicationSectionFragment;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.util.Log;

public class Parse extends Thread{

	private LocationManager locationManager;
	private String mocLocationProvider;
	public byte[] payloadmsg;
	public Boolean[] payb = new Boolean[48];
	public byte[] payc = new byte[6];
	public boolean new_data_flag = false;
	public boolean run_flag = true;
	long x, y, x_old = 50, y_old = 50, msg, predef;
	double x_init, y_init, x_res, y_res, depth;
	private String LOG_TAG = "PARSE:";
	private CommunicationSectionFragment bc;
	String mssg = "";

	public Parse(LocationManager locationManager, String mocLocationProvider, CommunicationSectionFragment chat) throws IOException {
		bc = chat;
		this.locationManager = locationManager;
	    this.mocLocationProvider = mocLocationProvider;
	}
	
	public void run() {
		while(run_flag) {
			synchronized(this) {
				try {
					this.wait();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			payc = payloadmsg;

			
			for(int i = 0; i < 6; i++) {
				Log.e("payc", Byte.toString(payc[i]));
				for(int j = 0; j < 8; j++) {
					if ((payc[i] & (1 << j)) != 0)
						payb[(5-i)*8+j] = true;
					else
						payb[(5-i)*8+j] = false;
				}
			}
			msg_type((short) msg_build(44, 4));			
			bc.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					if (!mssg.isEmpty())
						bc.mConversationArrayAdapter.add(mssg);
					 bc.getActivity().getActionBar().setTitle("Received Message: " + mssg);
					mssg = "";
				}
			});

			new_data_flag = false;
		}
	}

	private void msg_type(short m_type) {
		Log.e("m_type", Short.toString(m_type));
		switch (m_type) {
		case 1:
			x = msg_build(22,22); //lat ddmm.mm
			y = msg_build(0,22); //long dddmm.mm
			x_init = x/10000 + (x % 10000.0) / 6000 - 90; // u x_init je lat u decimalnim stupnjevima do tocnosti 2 decimale minute ddmm.mm --> dd.ddddddd...
			y_init = y/10000 + (y % 10000.0) / 6000 - 180; // u y_init je lon u decimalnim stupnjevima do tocnosti 2 decimale minute ddmm.mm --> dd.ddddddd...
			x_old = 50;
			y_old = 50;
			break;
		case 2:
			x = msg_build(19,18); // za case 2 x je lat u decimalnim minutama, samo do jedinice minute, do tocnosti 4 decimale minute m.mmmm
			y = msg_build(1,18); // za case 2 y je lon u decimalnim minutama, samo do jedinice minute, do tocnosti 4 decimale minute m.mmmm
			if ((x/10000.0 - ((x_init*60) % 10)) < -5)
				x_res = (Math.floor(x_init*6+1) + x/100000.0)/6;
			else if ((x/10000.0 - ((x_init*60) % 10)) > 5)
				x_res = (Math.floor(x_init*6-1) + x/100000.0)/6;
			else
				x_res = (Math.floor(x_init*6) + x/100000.0)/6;
			if ((y/10000.0 - ((y_init*60) % 10)) < -5)
				y_res = (Math.floor(y_init*6+1) + y/100000.0)/6;
			else if ((y/10000.0 - ((y_init*60) % 10)) > 5)
				y_res = (Math.floor(y_init*6-1) + y/100000.0)/6;
			else
				y_res = (Math.floor(y_init*6) + y/100000.0)/6;
			new_pos(x_res, y_res);
			x_old = (long) (x_res*60%0.01*10000);
			y_old = (long) (y_res*60%0.01*10000);
			x_init = (Math.floor(x_res*6000)) / 6000.0;
			y_init = (Math.floor(y_res*6000)) / 6000.0;
			depth = msg_build(37,7)/2.0;
			mssg = "Depth = " + Double.toString(depth) + "m.";
			// 1 msg To-Do
			break;
		case 3:
			x = msg_build(30,7); // za case 3 x je lat u decimalnim minutama, samo 3. i 4. decimala minute, _.__mm
			y = msg_build(23,7); // za case 3 y je lon u decimalnim minutama, samo 3. i 4. decimala minute, _.__mm
			if ((x - x_old) < -50)
				x_init += 0.01/60;
			if ((x - x_old) > 50)
				x_init -= 0.01/60;
			if ((y - y_old) < -50)
				y_init += 0.01/60;
			if ((y - y_old) > 50)
				y_init -= 0.01/60;
			x_old = x;
			y_old = y;
			depth = msg_build(37,7)/2.0;
			x_res = x_init + x/10000.0/60;
			y_res = y_init + y/10000.0/60;
			new_pos(x_res, y_res);
			for (int k = 0; k < 3; k++) {
				mssg += String.valueOf(Character.forDigit((int) msg_build(k*6,6), 35));
			}
			mssg = new StringBuffer(mssg).reverse().toString();
			mssg += ", Depth = " + Double.toString(depth) + "m.";
			break;
		case 4:
			break;
		case 5:
			x = msg_build(23,14);
			y = msg_build(9,14);
			x_old = x % 100;
			y_old = y % 100;
			if ((x/10000.0 - (x_init*60)%1) < -0.5)
				x_res = Math.floor(x_init*60 + 1) + x/10000.0;
			else if ((x/10000.0 - (x_init*60)%1) > 0.5)
				x_res = Math.floor(x_init*60 - 1) + x/10000.0;
			else
				x_res = Math.floor(x_init*60) + x/10000.0;
			if ((y/10000.0 - (y_init*60)%1) < -0.5)
				y_res = Math.floor(y_init*60 + 1) + y/10000.0;
			else if ((y/10000.0 - (y_init*60)%1) > 0.5)
				y_res = Math.floor(y_init*60 - 1) + y/10000.0;
			else
				y_res = Math.floor(y_init*60) + y/10000.0;
			new_pos(x_res, y_res);
			x_init = (Math.floor(x_res*6000)) / 6000.0;
			y_init = (Math.floor(y_res*6000)) / 6000.0;
			depth = msg_build(37,7)/2.0;
			predef = msg_build(4,5);
			mssg += "Depth = " + Double.toString(depth) + "m." + " Poruka br. " + Long.toString(predef);
			break;
		case 6:
			
			break;
		case 7:
			break;
		case 8:
			
			break;
		case 9:
			
			break;
		case 10:
			
			break;
		case 11:
			
			break;
		case 12:
			
			break;
		case 13:
			
			break;
		case 14:
			
			break;
		case 15:
			
			break;
		default:
			Log.e(LOG_TAG, "NIJE DOBRO!!!");
		}
			
		
	}
	
	private long msg_build(int from, int size) {
		long msg = 0;
		for (int i = from; i < (from + size); i++) {
			long value = (payb[i] ? 1 : 0) << (i - from);
			msg = msg | value;
		}
		return msg;
	}
	
	private void new_pos(double latitude, double longitude) {
		Location location = new Location(mocLocationProvider);
    	location.setLatitude(latitude);
    	location.setLongitude(longitude);
    	location.setAltitude(0.0);
    	location.setProvider(mocLocationProvider);
    	location.setTime(System.currentTimeMillis());
    	Log.e(LOG_TAG, location.toString());

    	locationManager.setTestProviderStatus(mocLocationProvider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
    	locationManager.setTestProviderLocation(mocLocationProvider, location);
	}
}