package com.ivailozn.trackme;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.fi;
import com.google.android.gms.internal.lo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity implements LocationListener, OnMapLongClickListener {

	private static final String KEY_MAP_TYPE = "KEY_MAP_TYPE";
	private static final String KEY_MARKER_TYPE = "KEY_MARKER_TYPE";
	private static LocationManager locationManager;
	private static String provider;

	private static TextView latituteField;
	private static TextView longitudeField;
	private static TextView time;
	private static TextView speed;
	private static TextView accuracy;
	private static TextView providerS;

	// private static TextView elements;

	private GoogleMap mMap;	
	private int markerType; 

	
	private Session session;
	private int lapDisplayCounter = 0;
	
	
	// market counter
	int marketCounter = 0;
	
	// start/stop marker, region markers. Always M0 is the start stop marker.
	private ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();
	int lastLoadedLocationsKeyIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		elements = (TextView) findViewById(R.id.elements);
		latituteField = (TextView) findViewById(R.id.TextView02);
		longitudeField = (TextView) findViewById(R.id.TextView04);
		time = (TextView) findViewById(R.id.TextView05);
		speed = (TextView) findViewById(R.id.TextView06);
		accuracy = (TextView) findViewById(R.id.TextView07);
		providerS = (TextView) findViewById(R.id.TextView08);

		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Define the criteria how to select the locatioin provider -> use
		// default criteria and provider
//		Criteria criteria = new Criteria();							    
//		provider = locationManager.getBestProvider(criteria, false);
//		Location location = locationManager.getLastKnownLocation(provider);
		
		// GPS provider - not tested yet on my live devices
//		provider = LocationManager.GPS_PROVIDER; 
//		Location location = locationManager.getLastKnownLocation(provider);
		
		// criteria with the best Accuracy possible.
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
		
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
		// Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");

			onLocationChanged(location);
		} else {
			latituteField.setText("Location not available");
			longitudeField.setText("Location not available");
		}

		// Prompt the user to Enabled GPS
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

		// mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// mMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)).title("Hello world"));

		if (mMap == null) {
			Toast.makeText(this, "mmap == null", Toast.LENGTH_SHORT).show();
			return;

		}

		mMap.setMyLocationEnabled(true);

//		mMap.addMarker(new MarkerOptions().position(new LatLng(43.0827, 25.6337)).title("Hello world"));
//		mMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)).title("Hello world"));

		setMapType(PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_MAP_TYPE, 3));
		setMarkerType(PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_MARKER_TYPE, 0));
		
		mMap.setOnMapLongClickListener(this);
	}

	/**
	 * 
	 * @param lat
	 * @param lng
	 * @param title
	 * @param markerType - 0 - default marker
	 * @return
	 */
	public MarkerOptions getMarker(double lat, double lng, String title, int markerType) {
		MarkerOptions mo = new MarkerOptions();
		if(markerType == 0)
			mo.position(new LatLng(lat, lng)).title(title);
		else if(markerType == 1) // 32 blue 
			mo.position(new LatLng(lat, lng)).title(title)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_32_blue));
		else if(markerType == 2) // 16 blue
			mo.position(new LatLng(lat, lng)).title(title)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_16_blue));		
		return mo;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		} else
		if (id == R.id.action_debug) {
			showDebugMenu();
			return true;
		 	
		}  else if (id == R.id.action_clearPointMarkers) {
			try {
				mMap.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (id == R.id.action_clearMarkers) {
			markers.clear();
			try {
				mMap.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (id == R.id.action_mapType) {

			setMapType();

			return true;
		}  else if (id == R.id.action_markerType) {

			setMarkerType();

			return true;
		} else if (id == R.id.action_calculateLaps) {

			// for i from all the point, check which is the near the M0 marker
			Set<String> keys = ((App) getApplication()).getModel().getLocationMaps().keySet();
	
			if (keys.isEmpty()) {
				Toast.makeText(this, "List is empty", Toast.LENGTH_SHORT).show();
				//break;
				return true;
			}
	
			final ArrayList<String> a = new ArrayList<String>();
//			final ArrayList<String> aDisplay = new ArrayList<String>();
//			a.addAll(keys);
			for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				a.add(string);
//				int pointsSize = ((ArrayList<MyLocation>) ((App) getApplication()).getModel().getLocationMaps().get(string)).size();
//				String date = new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong(string)));
//				aDisplay.add(" " + date + " " + pointsSize);
			}
					
			ArrayList<MyLocation> locs = ((ArrayList) ((App) getApplication()).getModel().getLocationMaps().get(a.get(lastLoadedLocationsKeyIndex)));	
			calculateLaps(locs);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showDebugMenu() {
		
		ArrayList<String> types = new ArrayList<String>();
		types.add("Dump Locations");
		types.add("Clear Locations");
		types.add("Fill Demo Data");
		types.add("Fill Demo Lap Data");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Debug Menu").setItems(types.toArray(new String[types.size()]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int which) {

				switch (which) {
				case 0:
					dumpLocations();
					break;

				case 1:
					// clear locations
					((App) getApplication()).getModel().getLocationMaps().clear();
					break;
					
				case 2:
					fillDemoData();
					break;
				case 3:
					fillDemoLapData();
					break;
				case 4:
					dumpLocations();
					break;
				default:
					break;
				}
				// display it as overlays with point
//				runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						try {
//							setMarkerType(which);
//						} catch (Exception e) {
//							e.printStackTrace();
//							Log.w(MainActivity.class.toString(), "\t" + e.getMessage());
//						}
//					}				
//				});
			}
		});
		builder.create();
		builder.show();
		
//		if (id == R.id.action_dumpLocations) {
//			dumpLocations();
//			return true;
//			}
//		 else if (id == R.id.action_clearLocations) {
//				((App) getApplication()).getModel().getLocationMaps().clear();
//				return true;
//			}else if (id == R.id.action_fillDemoData) {
//
//				fillDemoData();
//
//				return true;
//			}  else if (id == R.id.action_fillDemoLapData) {
//
//				fillDemoLapData();
//
//				return true;
//			} 
	}
	
	public void setMarkerType() {
		// display a list keys
		ArrayList<String> types = new ArrayList<String>();
		types.add("Default");
		types.add("32 Blue");
		types.add("16 Blue");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Marker Type").setItems(types.toArray(new String[types.size()]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int which) {

				// display it as overlays with point
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							setMarkerType(which);
						} catch (Exception e) {
							e.printStackTrace();
							Log.w(MainActivity.class.toString(), "\t" + e.getMessage());
						}
					}				
				});
			}
		});
		builder.create();
		builder.show();
	}
	
	public void setMarkerType(final int which) {

		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(KEY_MARKER_TYPE, which).commit(); 
		this.markerType = which;
	}
	
	public void setMapType() {
		// display a list keys
		ArrayList<String> types = new ArrayList<String>();
		types.add("None");
		types.add("Hybrid");
		types.add("Normal");
		types.add("Satallite");
		types.add("Terrain");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Map Type").setItems(types.toArray(new String[types.size()]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int which) {

				// display it as overlays with point
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							setMapType(which);
						} catch (Exception e) {
							e.printStackTrace();
							Log.w(MainActivity.class.toString(), "\t" + e.getMessage());
						}
					}

				
				});
			}
		});
		builder.create();
		builder.show();
	}

	public void setMapType(final int which) {
		if(mMap == null) return;
		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(KEY_MAP_TYPE, which).commit(); 
		switch (which) {
		case 0:
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			break;
		case 1:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case 2:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case 3:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case 4:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		default:
			break;
		}
	}
	
	public void fillDemoData() {
		((App) getApplication()).getModel().getLocationMaps().clear();
		((App) getApplication()).getModel().setLocations(new ArrayList<MyLocation>());
		String time = null;
		ArrayList<MyLocation> locations = null;
		Location l;

		// first object
		time = "1406128393000";
		locations = new ArrayList<MyLocation>();
		l = new Location("");
		l.setLatitude(43.0827);
		l.setLongitude(25.6337);
		locations.add(new MyLocation(l));
		l = new Location("");
		l.setLatitude(43.0828);
		l.setLongitude(25.6337);
		locations.add(new MyLocation(l));
		l = new Location("");
		l.setLatitude(43.0829);
		l.setLongitude(25.6337);
		locations.add(new MyLocation(l));
		l = new Location("");
		l.setLatitude(43.0830);
		l.setLongitude(25.6337);
		locations.add(new MyLocation(l));

		((App) getApplication()).getModel().getLocationMaps().put(time, locations);

		// second object
		time = "1406128593000";
		locations = new ArrayList<MyLocation>();
		for (int i = 0; i < 100; i++) {
			l = new Location("");
			l.setLatitude(43.0827 + (double) i / 1000);
			l.setLongitude(25.6337);
			locations.add(new MyLocation(l));
		}
		((App) getApplication()).getModel().getLocationMaps().put(time, locations);

		// invoke on location change
		for (int i = 0; i < 100; i++) {
			l = new Location("");
			l.setLatitude(43.0837 + (double) i / 1000);
			l.setLongitude(25.6337);
			onLocationChanged(l);
		}

	}
	
	private MyLocation getMyLocation(double lat, double lng, float speed, long time) {
		Location l = new Location("");
		l.setLatitude(lat);
		l.setLongitude(lng);
		l.setSpeed(speed);
		l.setTime(time);
		return new MyLocation(l);
	}
	
	public void fillDemoLapData() {
		((App) getApplication()).getModel().getLocationMaps().clear();
		((App) getApplication()).getModel().setLocations(new ArrayList<MyLocation>());
		String time = null;
		ArrayList<MyLocation> locations = null;
		Location l;

		// first object
		long starTime = System.currentTimeMillis();
		float speed = 20;
		time = "" + starTime;
		locations = new ArrayList<MyLocation>();
		// lap 1
		locations.add(getMyLocation(43.0830, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0831, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0832, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0833, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0834, 25.6337, speed, starTime += 100));

		locations.add(getMyLocation(43.0835, 25.6338, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6339, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6340, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6341, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6342, speed, starTime += 100));

		locations.add(getMyLocation(43.0835, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0834, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0833, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0832, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0831, 25.6342, speed, starTime += 100));

		locations.add(getMyLocation(43.0830, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6341, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6340, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6338, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6337, speed, starTime += 100));

		// lap 2
		locations.add(getMyLocation(43.0830, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0831, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0832, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0833, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0834, 25.6337, speed, starTime += 100));

		locations.add(getMyLocation(43.0835, 25.6338, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6339, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6340, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6341, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6342, speed, starTime += 100));

		locations.add(getMyLocation(43.0835, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0834, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0833, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0832, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0831, 25.6342, speed, starTime += 100));

		locations.add(getMyLocation(43.0830, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6341, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6340, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6338, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6337, speed, starTime += 100));

		// lap 3
		locations.add(getMyLocation(43.0830, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0831, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0832, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0833, 25.6337, speed, starTime += 100));
		locations.add(getMyLocation(43.0834, 25.6337, speed, starTime += 100));

		locations.add(getMyLocation(43.0835, 25.6338, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6339, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6340, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6341, speed, starTime += 100));
		locations.add(getMyLocation(43.0835, 25.6342, speed, starTime += 100));

		locations.add(getMyLocation(43.0835, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0834, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0833, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0832, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0831, 25.6342, speed, starTime += 100));

		locations.add(getMyLocation(43.0830, 25.6342, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6341, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6340, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6338, speed, starTime += 100));
		locations.add(getMyLocation(43.0830, 25.6337, speed, starTime += 100));

		((App) getApplication()).getModel().getLocationMaps().put(time, locations);

		// second object
//		time = "1406128593000";
//		locations = new ArrayList<MyLocation>();
//		for (int i = 0; i < 100; i++) {			
//			locations.add(getMyLocation(43.0827 + (double) i / 1000, 25.6337, speed));
//		}
		
//		((App) getApplication()).getModel().getLocationMaps().put(time, locations);

		
//		// invoke on location change
//		for (int i = 0; i < 100; i++) {
//			l = new Location("");
//			l.setLatitude(43.0837 + (double) i / 1000);
//			l.setLongitude(25.6337);
//			onLocationChanged(l);
//		}

	}

	private void dumpLocations() {
		Log.w(MainActivity.class.toString(), "---dumpLocations");
		Set<String> keys = ((App) getApplication()).getModel().getLocationMaps().keySet();

		if (keys.isEmpty()) {
			Toast.makeText(this, "List is empty", Toast.LENGTH_SHORT).show();
			return;
		}

		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();

			int pointsSize = ((ArrayList) ((App) getApplication()).getModel().getLocationMaps().get(string)).size();

			String date = getDate(string);
			Log.w(MainActivity.class.toString(), "" + string + " " + date + " " + pointsSize);
			Iterator it = (Iterator) ((App) getApplication()).getModel().getLocationMaps().get(string).iterator();
			// int i = 0;
			// for (; it.hasNext();) {
			// Location location = ((MyLocation) it.next()).getLocatoin();
			// Log.w(MainActivity.class.toString(), "   " + i++ + " " + location.getLatitude() + " " + location.getLongitude());
			// }
		}
	}

	/**
	 * Normally the keys in our data storage are the datetime in long format, but we added a user description in the plain text
	 * so the format it 
	 * 			123412341234 UserTitle  with splace delimiter
	 * @param key
	 * @return
	 */
	public String getDate(String key) {
		return key.indexOf(" ") == -1 
				? new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong(key))) 
				: 
//					 try {
					new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong( key.substring(0, key.indexOf(" ")) )))
						+ key.substring(key.indexOf(" "));
//		return new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong(key)));
	}

	@Override
	public void onLocationChanged(final Location location) {

		if (location == null)
			return;

		// synchronized (location) {
		((App) getApplication()).getModel().getLocations().add(new MyLocation(location));
		// }

		latituteField.setText(String.format("%.4f", location.getLatitude()));
		longitudeField.setText(String.format("%.4f", location.getLongitude()));
		time.setText(String.format("%s", new SimpleDateFormat("HH:mm:ss").format(new Date(location.getTime()))));
		speed.setText(String.format("%.4f", location.getSpeed() * 3.6));// ms over ground ms*3.6 = kmh
		accuracy.setText(String.format("%.4f", location.getAccuracy()));
		providerS.setText(String.format("%s", location.getProvider()));

//		try {
//			elements.setText("Elements: " + ((App) getApplication()).getModel().getLocations().size() + location.getExtras().toString());
//		} catch (Exception e) {
//		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try {
					if (mMap == null)
						return;
					mMap.addMarker(
							getMarker(location.getLatitude(), location.getLongitude(), 
									"" + ((App) getApplication()).getModel().getLocations().size()
									+ " " + String.format("%.1f", location.getSpeed() * 3.6), 
									markerType));
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), mMap.getCameraPosition().zoom));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(MainActivity.class.toString(), e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}

	protected void onResume() {
		super.onResume();
		// requestUpdates();
	}
	
	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		// removeUpdates();
	}
	
	public void startStop(View v) {
		if(getString(R.string.label_start).equals(((Button)findViewById(R.id.startStop)).getText())) {
			requestUpdates();
		} else {
			removeUpdates();
		}
	}
	
	public void requestUpdates() {		
		locationManager.requestLocationUpdates(provider, 100, 1, this);		
		findViewById(R.id.table).setBackgroundColor(Color.GREEN);
		((Button)findViewById(R.id.startStop)).setText(R.string.label_stop);
	}

	public void removeUpdates() {
		locationManager.removeUpdates(this);
		findViewById(R.id.table).setBackgroundColor(Color.LTGRAY);
		((Button)findViewById(R.id.startStop)).setText(R.string.label_start);
	}

	/**
	 * Instances of static inner classes do not hold an implicit reference to their outer class.
	 */
	private class MyHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public MyHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity activity = mActivity.get();
			if (activity != null) {
				// ...
			}
		}
	}

	private final MyHandler mHandler = new MyHandler(this);

	/**
	 * Instances of anonymous classes do not hold an implicit reference to their outer class when they are "static".
	 */
	private static final Runnable sRunnable = new Runnable() {
		@Override
		public void run() {
		}
	};

	public void onBackPressed() {
		Toast.makeText(this, "bye", 0).show();
		((App) getApplication()).exit();
		finish();
	};

	public void load(View v) {
		// build a list
		Set<String> keys = ((App) getApplication()).getModel().getLocationMaps().keySet();

		if (keys.isEmpty()) {
			Toast.makeText(this, "List is empty", Toast.LENGTH_SHORT).show();
			return;
		}

		final ArrayList<String> a = new ArrayList<String>();
		final ArrayList<String> aDisplay = new ArrayList<String>();
		// a.addAll(keys);
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			a.add(string);
			int pointsSize = ((ArrayList<MyLocation>) ((App) getApplication()).getModel().getLocationMaps().get(string)).size();
			
			// format timeinLong_userTitle
//			String date = string.indexOf(" ") == -1 
//					? getDate(string) 
//					: 
////						 try {
//						new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong( string.substring(0, string.indexOf(" ")) )))
////						} catch (Exception e) {return string;}
////						finally { return string;}
//					;
			aDisplay.add("" + getDate(string) + "(" + pointsSize+")");
		}

		// display a list keys
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an activity").setItems(aDisplay.toArray(new String[aDisplay.size()]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, final int which) {
				// The 'which' argument contains the index position
				// of the selected item

				// clear the view
				try {
					mMap.clear();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(MainActivity.class.toString(), e.getMessage(), e);
				}

				// display it as overlays with point
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {

							// Iterator iterator = ((App) getApplication()).getModel().getLocations().iterator();
							Iterator iterator = (Iterator) ((ArrayList) ((App) getApplication()).getModel().getLocationMaps().get(a.get(which))).iterator();
							lastLoadedLocationsKeyIndex = which;
							int points = ((ArrayList) ((App) getApplication()).getModel().getLocationMaps().get(a.get(which))).size();
							Log.w(this.toString(), "loading: " + points + " points");
							int i = 0;
							for (; iterator.hasNext();) {
								Location location = ((MyLocation) iterator.next()).getLocatoin();
								// center the map on the first point.
								if(i == 0) {
									mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), mMap.getCameraPosition().zoom));
								}
								mMap.addMarker(
										// new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("" + i++)
										getMarker(location.getLatitude(), location.getLongitude(), " " + i++ + " " + String.format("%.1f", location.getSpeed() * 3.6) ,
												markerType)
										);
								Log.w(this.toString(), "loading location: " + i + " " + location.getLatitude());
							}
							
							Toast.makeText(MainActivity.this, "Loaded " + i + " points", Toast.LENGTH_SHORT).show();
							// mMap.addMarker(new MarkerOptions().position(new LatLng(43.0827, 25.6337)).title("Hello world"));
							
							
						} catch (Exception e) {
							e.printStackTrace();
							Log.w(MainActivity.class.toString(), "\t" + e.getMessage());
						}
					}
				});
			}
		});
		builder.create();
		builder.show();
	}

	public void save(View v) {
		// get all the current points
		// put them in the Model - hashmap
		if (((App) getApplication()).getModel().getLocations().size() == 0) {
			Toast.makeText(this, "No Cue Points", Toast.LENGTH_SHORT).show();
			return;
		}

		final int points = ((App) getApplication()).getModel().getLocations().size();
		
		// ask for a name
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(String.format("Save Session Title(%d)", points));
		alert.setMessage("Enter name of the session");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
				String title = input.getText().toString();
		  	
				String key = "" + System.currentTimeMillis() + " " + title;
				
				// DEBUG
				if (((App) getApplication()).getModel().getLocationMaps().containsKey(key)) {
					Log.w(MainActivity.class.toString(), "storage contains" + key);
				} else {
					Log.w(MainActivity.class.toString(), "storage does not cintain" + key);
				}
	
				HashMap<String, ArrayList<MyLocation>> h = ((App) getApplication()).getModel().getLocationMaps();
				h.toString();
	
				Object o = ((App) getApplication()).getModel().getLocationMaps().put(key, ((App) getApplication()).getModel().getLocations());
				if (o == null) {
					Log.w(MainActivity.class.toString(), "new object put in the map");
				} else {
					Log.w(MainActivity.class.toString(), "object replaced");
				}
	
				if (((ArrayList<MyLocation>) ((App) getApplication()).getModel().getLocationMaps().get(key)).size() != points) {
					Toast.makeText(MainActivity.this, "Problem in writing points!", Toast.LENGTH_SHORT).show();
				}
	
				// DEBUG
				dumpLocations();
	
				// clear the view
				try {
					mMap.clear();
				} catch (Exception e) {
					e.printStackTrace();
					Log.w(MainActivity.class.toString(), "\t" + e.getMessage());
				}
	
				// clear the current arraylist with points object
				// ((App) getApplication()).getModel().getLocations().clear();
				((App) getApplication()).getModel().setLocations(new ArrayList<MyLocation>());
	
				Toast.makeText(MainActivity.this, "Saved " + points + " points!", Toast.LENGTH_SHORT).show();
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});

		alert.show();
	}

	public void reqUp(View v) {
		requestUpdates();
	}

	public void remUp(View v) {
		removeUpdates();
	}
	
	@Override
	public void onMapLongClick(LatLng point) {
		Toast.makeText(this, "Clicked on " + point + " point!", Toast.LENGTH_SHORT).show();
		MarkerOptions m = getMarker(point.latitude, point.longitude, "M" + marketCounter++, 0);
		m.draggable(true);
		mMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			
			@Override
			public void onMarkerDragStart(Marker marker) {
			}
			
			@Override
			public void onMarkerDragEnd(Marker marker) {
			}
			
			@Override
			public void onMarkerDrag(Marker marker) {
			}
		});
		mMap.addMarker(m);
		markers.add(m);
	}
	
	private void calculateLaps(ArrayList<MyLocation> locs) {
		if (markers.size() == 0 || markers.get(0) == null) {
			Toast.makeText(this, "Put start/stop marker at least!", Toast.LENGTH_SHORT).show();
			return;
		}
		
//		Iterator<MyLocation> iterator = locs.iterator();
//		for (; iterator.hasNext();) {
//			MyLocation l = (MyLocation) iterator.next();
//			
//			// get distance
//			Log.w(this.toString(), "distance " + getDistance(markers.get(0), l));
//		}
		Iterator iterator = (Iterator) (locs).iterator();
		int i = 0;
		int lapCounter = 0;
		int curentPoint = 0;
		
		session = new Session("new session");
		Lap lap = new Lap(""+ lapCounter);
		session.getLaps().add(lap);
		lapDisplayCounter = 0;
		
		// print all points for DEBUG purposes
		for (Iterator it = locs.iterator(); it.hasNext();) {
			MyLocation f = (MyLocation) it.next();
			double d1 = getDistance(markers.get(0), f);
			Log.w(this.toString(), "distance " + " point: " + i++ + " " + String.format("%f.2", d1));
		}
		
		i = 0;
		
		// first second and third points
		MyLocation f = null, s = null, t = null; 
		for (; iterator.hasNext();) {
			try {
				if (f == null && s == null && t == null) {
					// init first 3 point
					f = (MyLocation) iterator.next();
					s = (MyLocation) iterator.next();
					t = (MyLocation) iterator.next();

				} else {
					// then we move forward point by point
					f = s;
					s = t;
					t = (MyLocation) iterator.next();
				}
			} catch (NoSuchElementException e) {
				System.out.println("end of the points");
			}

			// get distance
			double d1 = getDistance(markers.get(0), f);
			if (s == null || t == null)
				return;
			double d2 = getDistance(markers.get(0), s); // our main point.
			double d3 = getDistance(markers.get(0), t);

//			double min;
			double m = measure(markers.get(0).getPosition().latitude, markers.get(0).getPosition().longitude, s.getLatitude(), s.getLongitude());
			double START_STOP_DISTANCE = 10;
			if (d1 > d2 && d2 < d3 && m <= START_STOP_DISTANCE) {
//			if (d1 > d2 && d2 < d3) {
				System.out.println("we have a lap point(start)" + String.format("d: %.6f %.6f %.6f ", d1, d2, d3));
				lapCounter++;
				curentPoint = 0;
				lap = new Lap("" + lapCounter);
				session.getLaps().add(lap);
			}
			lap.getPoints().add(s);			 
			Log.w(this.toString(), "distance " + " lap: " + lapCounter + " curentPoint: " + curentPoint++ + " point: " + i++ + " " + String.format("d: %.6f %.6f %.6f ", d1, d2, d3));
		}
		
		debugSession(session);
		
		// diplay laps
		displayLaps();
	}

	public void displayLaps() {
		if (mMap != null) {
			mMap.clear();

			
			
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					
					double lapDistance = 0;
					double lapTime = 0;
					MyLocation startingLocation = null;
					
					
					findViewById(R.id.lapNavigatorPanel).setVisibility(View.VISIBLE);
					
					// display markers
					try {
						for(int p = 0; p < markers.size(); p++) {
							mMap.addMarker(getMarker(markers.get(p).getPosition().latitude, markers.get(p).getPosition().longitude, "m" + p, 0));
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(MainActivity.class.toString(), e.getMessage(), e);
					}
					
					try {
						((TextView)findViewById(R.id.lapInfo)).setText("" + lapDisplayCounter + "/" + session.getLaps().size());
						MyLocation location;
						int size = session.getLaps().get(lapDisplayCounter).getPoints().size();
						for (int k = 0; k < size; k++) {
							location = session.getLaps().get(lapDisplayCounter).getPoints().get(k);
							// center the map on the first point
							if (k == 0) {
								startingLocation = location;
								mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),  mMap.getCameraPosition().zoom));
							}
							mMap.addMarker(getMarker(location.getLatitude(), location.getLongitude(), "" + k + " " + String.format("%.1f", location.getSpeed() * 3.6), markerType));
							if((k+1) == size) {
								lapDistance = measure(startingLocation.latitude, startingLocation.longitude, location.latitude, location.longitude);
							}
							
							long millis = location.getTime() - startingLocation.getTime();
							long second = (millis / 1000) % 60;
							long minute = (millis / (1000 * 60)) % 60;
							long hour = (millis / (1000 * 60 * 60)) % 24;							
							String time = String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);						
							((TextView) findViewById(R.id.lapTime)).setText("LapTime: " + time);
							((TextView) findViewById(R.id.lapDistance)).setText("LapDistance: " + lapDistance);
						}
						
						
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(MainActivity.class.toString(), e.getMessage(), e);
					}
				}
			});
		}
	}
	
	private void debugSession (Session session) {
		Log.w(this.toString(), "DEBUG SESSION");
		Log.w(this.toString(), "Name: " + session.getName());
		Log.w(this.toString(), "LapsSize: " + session.getLaps().size());
		for (Iterator iterator = session.getLaps().iterator(); iterator.hasNext();) {
			Lap lap = (Lap) iterator.next();
			Log.w(this.toString(), "Laps: " + lap.getName() + " size: " +  lap.getPoints().size());
		}
	}
	
	double getDistance (MarkerOptions m0, MyLocation point) {
		double d = 0;
		d = Math.sqrt(
				Math.pow((point.getLatitude()- m0.getPosition().latitude), 2) +
				Math.pow((point.getLongitude()- m0.getPosition().longitude), 2)
				);
		return d;
	}
	
	public void back(View v) {
		lapDisplayCounter --;
		displayLaps();
	}
	
	public void forward(View v) {
		lapDisplayCounter++;
		displayLaps();
	}
	
	public void hideLapsNavigator(View v) {
		findViewById(R.id.lapNavigatorPanel).setVisibility(View.GONE);
	}
	
	// measure distance in meters from two points
	double measure(double lat1, double  lon1, double  lat2, double  lon2){  // generally used geo measurement function
		double  R = 6378.137; // Radius of earth in KM
		double  dLat = (lat2 - lat1) * Math.PI / 180;
		double  dLon = (lon2 - lon1) * Math.PI / 180;
		double  a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
		double  c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double  d = R * c;
	    return d * 1000; // meters
	}
}
