package com.fer.labust.diverassistant;

import com.fer.labust.lawnmower.model.Point;
import com.fer.labust.lawnmower.model.Poligon;
import com.fer.labust.lawnmower.services.PlannerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import labust.tritech.DefaultMessage;
import labust.tritech.DefaultMessageList;
import labust.tritech.MmcMessage;
import labust.tritech.MtMessage;

import com.fer.labust.diverassistant.BluetoothChatService;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MarkerOptionsCreator;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */

	/*
	 * Dodavanje pinova na dugi klik + Brisanje markera na click + Duže
	 * ostajanje budnim + Alert sve jedinice + Prikaz dubine na vrh Povecat
	 * listu + Prebacit spajanje unutar taba Recovery za pale konekcije maknut
	 * support
	 * brisanje markera (kad makne jedan maknut poligon)
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final long DELAY = 1800000L;
	public static boolean D = true;
	private static final String TAG = "DiverAssistantMainActivity";

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */

	ViewPager mViewPager;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (D)
			Log.e(TAG, "+++ ON CREATE OPTIONS MENU +++");
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);

	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.alert:
			if (CommunicationSectionFragment.chatSetup()) {
				CommunicationSectionFragment.getInstance().sendMessage("A");
			} else {
				Toast.makeText(this, "Device not connected", 10).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the two
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		Settings.System.putLong(getContentResolver(),
				Settings.System.SCREEN_OFF_TIMEOUT, MainActivity.DELAY);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		if (tab.getPosition() == 1) {
			getActionBar().setTitle("");
		}
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new Fragment();
			switch (position) {
			case 0:
				fragment = new MapSectionFragment();
				break;
			case 1:
				fragment = new CommunicationSectionFragment();
				break;
			case 2:
				fragment = new AscentSectionFragment();
				break;
			}

			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.map_section).toUpperCase(l);
			case 1:
				return getString(R.string.communication_section).toUpperCase(l);
			case 2:
				return getString(R.string.ascent_section).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class CommunicationSectionFragment extends Fragment implements
			LocationListener {
//			, SensorEventListener {
		private static final String TAG = "DiverAssistantCommunicationSectionFragment";
		private static final boolean D = true;

		// Message types sent from the BluetoothChatService Handler
		public static final int MESSAGE_STATE_CHANGE = 1;
		public static final int MESSAGE_READ = 2;
		public static final int MESSAGE_WRITE = 3;
		public static final int MESSAGE_DEVICE_NAME = 4;
		public static final int MESSAGE_TOAST = 5;

		public static boolean chatSetup = false;
		// Key names received from the BluetoothChatService Handler
		public static final String DEVICE_NAME = "device_name";
		public static final String TOAST = "toast";

		// Intent request codes
		private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
		private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
		private static final int REQUEST_ENABLE_BT = 3;

		// Layout Views
		private ListView mConversationView;
		private ListView mPredefinedMessagesView;
		private EditText mOutEditText;
		private Button mSendButton;
		private static CommunicationSectionFragment instance;

		// Name of the connected device
		private String mConnectedDeviceName = "Ground";
		// Array adapter for the conversation thread
		public ArrayAdapter<String> mConversationArrayAdapter;
		public ArrayAdapter<DefaultMessage> mPredefinedMessagesArrayAdapter;
		// String buffer for outgoing messages
		private StringBuffer mOutStringBuffer;
		// Local Bluetooth adapter
		private BluetoothAdapter mBluetoothAdapter = null;
		// Member object for the chat services
		private BluetoothChatService mChatService = null;

		private static final long MIN_TIME = 1000;
		private static final float MIN_DISTANCE = 1;
		private SensorManager mSensorManager;
		private Sensor mOrientation;
		private boolean firstTime = true;
		private Parse parse;
		private GoogleMap map;
		private LocationManager locationManager;
		private LatLng ltn;
		private float oriX;
		private CameraPosition campos;

		private String mocLocationProvider = LocationManager.GPS_PROVIDER;

		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */

		public CommunicationSectionFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (D)
				Log.e(TAG, "+++ ON CREATE VIEW +++");
			View rootView = inflater.inflate(R.layout.fragment_communication,
					container, false);
			map = ((SupportMapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			locationManager = (LocationManager) getActivity().getSystemService(
					Context.LOCATION_SERVICE);
			locationManager.addTestProvider(mocLocationProvider, false, false,
					false, false, false, false, false, 0, 0);

			locationManager.setTestProviderEnabled(mocLocationProvider, true);
			locationManager.requestLocationUpdates(mocLocationProvider, 0l, 0f,
					this);
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			ltn = new LatLng(0, 0);
			mSensorManager = (SensorManager) getActivity().getSystemService(
					SENSOR_SERVICE);
			mOrientation = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			try {
				parse = new Parse(locationManager, mocLocationProvider, this);
				parse.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return rootView;
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
			// Inflate the menu; this adds items to the action bar if it is
			// present.
			if (D)
				Log.e(TAG, "+++ ON CREATE OPTIONS MENU +++");
			getActivity().getMenuInflater().inflate(R.menu.communication_menu,
					menu);
			super.onCreateOptionsMenu(menu, menuInflater);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (D)
				Log.e(TAG, "+++ ON OPTIONS SELECTED MENU +++");
			Intent serverIntent = null;
			switch (item.getItemId()) {
			case R.id.connect_communication:
				// Launch the DeviceListActivity to see devices and do scan
				if (D)
					Log.e(TAG,
							"+++ ON OPTIONS SELECTED MENU, connect communication +++");
				serverIntent = new Intent(getActivity(),
						DeviceListActivity.class);
				startActivityForResult(serverIntent,
						DeviceType.COMMUNICATION.getValue());
				return true;
			case R.id.connect_depth_sensor:
				// Launch the DeviceListActivity to see devices and do scan
				if (D)
					Log.e(TAG,
							"+++ ON OPTIONS SELECTED MENU, connect depth +++");
				serverIntent = new Intent(getActivity(),
						DeviceListActivity.class);
				startActivityForResult(serverIntent,
						DeviceType.DEPTH_SENSOR.getValue());
				return true;
			}
			return false;
		}

		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			if (D)
				Log.d(TAG, "onActivityResult " + resultCode);

			if (requestCode == DeviceType.COMMUNICATION.getValue()) {
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data, true, DeviceType.COMMUNICATION);
				}
			}
			if (requestCode == DeviceType.DEPTH_SENSOR.getValue()) {
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data, false, DeviceType.DEPTH_SENSOR);
				}
			}

		}

		@Override
		public void onStart() {
			super.onStart();
			if (D)
				Log.e(TAG, "++ ON START ++");
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Activity activity = getActivity();
				Toast.makeText(activity, "Bluetooth is not available",
						Toast.LENGTH_LONG).show();
				return;
			}
			// If BT is not on, request that it be enabled.
			// setupChat() will then be called during onActivityResult
			Log.d(TAG, "bluetooth - " + mBluetoothAdapter.isEnabled());
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				// startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				// connectDevice(false);
				// Otherwise, setup the chat session

			} else {
				if (mChatService == null)
					setupChat();
			}
			if (mPredefinedMessagesArrayAdapter == null) {
				setupPredefinedMessages();
			}
			chatSetup = true;
			instance = this;
		}

		public static CommunicationSectionFragment getInstance() {
			return instance;
		}

		@Override
		public void onResume() {
			super.onResume();
//			mSensorManager.registerListener(this, mOrientation,
//					SensorManager.SENSOR_DELAY_NORMAL);
		}

		public static boolean chatSetup() {
			return chatSetup;
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			parse.run_flag = false;
			map.setMyLocationEnabled(false);
		}

		private void connectDevice(Intent data, boolean secure,
				DeviceType deviceType) {
			// Get the device MAC address
			String address = data.getExtras().getString(
					DeviceListActivity.EXTRA_DEVICE_ADDRESS);
			// Get the BluetoothDevice object
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			Log.d(TAG, "connectDevice - " + device.getName());
			mChatService.connect(device, secure, deviceType);

		}

		private void setupChat() {
			Log.d(TAG, "setupChat()");

			Activity activity = getActivity();
			// Initialize the array adapter for the conversation thread
			mConversationArrayAdapter = new ArrayAdapter<String>(activity,
					R.layout.message);
			mConversationView = (ListView) getView().findViewById(R.id.in);
			mConversationView.setAdapter(mConversationArrayAdapter);

			// Initialize the compose field with a listener for the return key
			mOutEditText = (EditText) getView()
					.findViewById(R.id.edit_text_out);
			// mOutEditText.setOnEditorActionListener(mWriteListener);

			// Initialize the send button with a listener that for click events
			mSendButton = (Button) getView().findViewById(R.id.button_send);
			mSendButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Send a message using content of the edit text widget
					TextView view = (TextView) getView().findViewById(
							R.id.edit_text_out);
					String message = view.getText().toString();
					sendMessage(message);
				}
			});

			// Initialize the BluetoothChatService to perform bluetooth
			// connections
			mChatService = new BluetoothChatService(getActivity(), mHandler);

			// Initialize the buffer for outgoing messages
			mOutStringBuffer = new StringBuffer("");
			// connectDevice(false);
		}

		private void setupPredefinedMessages() {
			Log.d(TAG, "setupPredifinedMessages()");
			Activity activity = getActivity();
			mPredefinedMessagesArrayAdapter = new ArrayAdapter<DefaultMessage>(
					activity, R.layout.predefined_message);
			mPredefinedMessagesView = (ListView) getView().findViewById(
					R.id.predefined_messages);
			mPredefinedMessagesView.setAdapter(mPredefinedMessagesArrayAdapter);
			DefaultMessageList defList = new DefaultMessageList();
			for (DefaultMessage defaultMessage : defList.getDefaultMessages()) {
				mPredefinedMessagesArrayAdapter.add(defaultMessage);
			}
			mPredefinedMessagesView
					.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							DefaultMessage message = mPredefinedMessagesArrayAdapter
									.getItem(position);
							sendMessage(String.valueOf(message
									.getMessageNumber()));
						}
					});

		}

		public void sendMessage(String message) {
			// Check that we're actually connected before trying anything
			if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
				Toast.makeText(getActivity(), R.string.not_connected,
						Toast.LENGTH_SHORT).show();
				return;
			}

			// Check that there's actually something to send
			if (message.length() > 0 && message.length() <= 6) {
				// Get the message bytes and tell the BluetoothChatService to
				// write
				byte[] send = message.getBytes();
				MtMessage msg = new MtMessage();
				msg.msgType = 78;
				msg.node = 86;
				msg.txNode = 255;
				msg.rxNode = 86;
				MmcMessage mmsg = new MmcMessage();
				mmsg.msgType = 33;
				mmsg.rx = 0;
				mmsg.tx = 2;
				mmsg.rxTmo = 3000;
				mmsg.payload = new byte[16];
				mmsg.payload[0] = (byte) (message.length() * 8);
				System.arraycopy(send, 0, mmsg.payload, 1, send.length);
				msg.payload = mmsg.serialize();
				mChatService.write(MtMessage.serializeHH(msg), message);

				// Reset out string buffer to zero and clear the edit text field
				mOutStringBuffer.setLength(0);
				mOutEditText.setText(mOutStringBuffer);
			}
		}

		// The Handler that gets information back from the BluetoothChatService
		private final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					if (D)
						Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1) {
					case BluetoothChatService.STATE_CONNECTED:
						// setStatus(getString(R.string.title_connected_to,
						// mConnectedDeviceName));
						mConversationArrayAdapter.clear();
						break;
					case BluetoothChatService.STATE_CONNECTING:
						// setStatus(R.string.title_connecting);
						break;
					case BluetoothChatService.STATE_LISTEN:
					case BluetoothChatService.STATE_NONE:
						// setStatus(R.string.title_not_connected);
						break;
					}
					break;
				case MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
					// construct a string from the buffer
					String writeMessage = new String(writeBuf);
					mConversationArrayAdapter.add("Me:  " + writeMessage);
					break;
				case MESSAGE_READ:
					MmcMessage readPay = (MmcMessage) msg.obj;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(new String(readPay.payload));
					parse.payloadmsg = readPay.payload;
					Log.e("payload", Byte.toString(readPay.payload[0]));
					Log.e("payload", Byte.toString(readPay.payload[1]));
					Log.e("payload", Byte.toString(readPay.payload[2]));
					Log.e("payload", Byte.toString(readPay.payload[3]));
					Log.e("payload", Byte.toString(readPay.payload[4]));
					Log.e("payload", Byte.toString(readPay.payload[5]));
					parse.new_data_flag = true;
					map.setMyLocationEnabled(true);
					synchronized (parse) {
						parse.notify();
					}

					Log.e(TAG, mConnectedDeviceName + ":  " + readMessage);
					break;
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					// mConnectedDeviceName =
					// msg.getData().getString(DEVICE_NAME);
					mConnectedDeviceName = "Ground";
					Activity activity = getActivity();
					Toast.makeText(activity,
							"Connected to " + mConnectedDeviceName,
							Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					Toast.makeText(getActivity(),
							msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
							.show();
					break;
				}
			}
		};

		@Override
		public void onLocationChanged(Location location) {
			if (D)
				Log.i(TAG, "on_location_changed: " + location.getAltitude()
						+ " " + location.getLongitude());
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			LatLng latLng = new LatLng(lat, lng);
			ltn = latLng;
			Log.e("ltn", latLng.toString());
			if (firstTime) {
				synchronized (this) {
					// Toast.makeText(this, "Location " +
					// lat+","+lng,Toast.LENGTH_SHORT).show();
					campos = new CameraPosition.Builder().target(ltn).zoom(10)
							.build();
					map.moveCamera(CameraUpdateFactory
							.newCameraPosition(campos));
				}
				firstTime = false;
			}
			// map.setMyLocationEnabled(true);
			// Toast.makeText(this, "Location " +
			// lat+","+lng,Toast.LENGTH_SHORT).show();
			// locationManager.removeUpdates(this);

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

//		@Override
//		public void onAccuracyChanged(Sensor sensor, int accuracy) {
//			// TODO Auto-generated method stub
//
//		}

//		@Override
//		public void onSensorChanged(SensorEvent sensorEvent) {
//			if (D)
//				Log.i(TAG, "onSensorChanged called ");
//			synchronized (this) {
//				if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//					oriX = sensorEvent.values[0];
//					campos = new CameraPosition.Builder(map.getCameraPosition())
//							.target(map.getCameraPosition().target)
//							.bearing(oriX).build();
//					map.moveCamera(CameraUpdateFactory
//							.newCameraPosition(campos));
//					// map.animateCamera(CameraUpdateFactory.newCameraPosition(campos));
//				}
//			}
//
//		}

	}

	public static class MapSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private GoogleMap map;
		private List<Point> points;
		private LocationManager locationManager;
		private static View view;
		private static Marker markerToDelete;
		private Button mDrawPath;

		public MapSectionFragment() {
		}

		public void setMarkerToDelete(Marker marker) {
			this.markerToDelete = marker;
		}

		public Marker getMarkerToDelete() {
			return this.getMarkerToDelete();
		}

		public void drawPath(){
			if(D) Log.i(TAG, "drawPath called ");
			List<Point> pathPoints = new ArrayList<Point>();
			PlannerService plannerService = new PlannerService(new Poligon(this.points), 5);
			pathPoints = plannerService.odrediOptimalneTockePresjeka();
//			if(D) Log.i(TAG, "drawPath" + pathPoints.toString());
			PolygonOptions polygonOptions = new PolygonOptions();
			for (Point point : this.points){
				polygonOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
			}
			polygonOptions.strokeColor(0xfff08080);
			polygonOptions.strokeWidth(4);
			map.addPolygon(polygonOptions);
			for (int i=0; i<pathPoints.size();i+=2){
				Point firstPoint = pathPoints.get(i);
				Point secondPoint = pathPoints.get(i+1);
				PolylineOptions polyLineOptions = new PolylineOptions();
				polyLineOptions.add(new LatLng(firstPoint.getLatitude(), firstPoint.getLongitude()),
						new LatLng(secondPoint.getLatitude(), secondPoint.getLongitude()));
				polyLineOptions.color(0xfff0e68c);
				polyLineOptions.width(2);
				map.addPolyline(polyLineOptions);
			}
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			this.points = new ArrayList<Point>();
			if (view != null) {
				ViewGroup parent = (ViewGroup) view.getParent();
				if (parent != null)
					parent.removeView(view);
			}
			try {
				view = inflater
						.inflate(R.layout.fragment_map, container, false);
				map = ((SupportMapFragment) getFragmentManager()
						.findFragmentById(R.id.map)).getMap();
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			} catch (InflateException e) {
				/* map is already there, just return view as it is */
			}

			map.setOnMapLongClickListener(new OnMapLongClickListener() {

				@Override
				public void onMapLongClick(LatLng arg0) {
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(arg0);
					map.addMarker(markerOptions);
					Point point = new Point(arg0.latitude, arg0.longitude);
					points.add(point);
				}
			});

			map.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker arg0) {
					MapSectionFragment.markerToDelete = arg0;
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								MapSectionFragment.markerToDelete.remove();
								break;

							case DialogInterface.BUTTON_NEGATIVE:
								// No button clicked
								break;
							}
						}

					};

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setMessage("Delete marker?")
							.setPositiveButton("Yes", dialogClickListener)
							.setNegativeButton("No", dialogClickListener)
							.show();
					return false;
				}
			});

			return view;
		}

		@Override
		public void onStart() {
			mDrawPath = (Button) getView().findViewById(R.id.button_draw_path);
			mDrawPath.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					drawPath();
				}
			});
			super.onStart();
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();

		}
	}

	public static class AscentSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public AscentSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_ascent,
					container, false);
			// TextView dummyTextView = (TextView) rootView
			// .findViewById(R.id.edit_text_out);
			// dummyTextView.setText("prob");
			return rootView;
		}

		public void onDestroyView() {
			try {
				// SupportMapFragment fragment = ((SupportMapFragment)
				// getFragmentManager().findFragmentById(R.id.map));
				// FragmentTransaction ft =
				// getActivity().getFragmentManager().beginTransaction();
				// ft.remove(fragment);
				// ft.commit();
			} catch (Exception e) {
			}
			super.onDestroyView();
		}
	}
}
