package com.ivailozn.trackme;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity implements LocationListener {

	private static LocationManager locationManager;
	private static String provider;

	private static TextView latituteField;
	private static TextView longitudeField;
	private static TextView time;
	private static TextView speed;
	private static TextView accuracy;
	private static TextView providerS;

	private static TextView elements;

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		elements = (TextView) findViewById(R.id.elements);
		latituteField = (TextView) findViewById(R.id.TextView02);
		longitudeField = (TextView) findViewById(R.id.TextView04);
		time = (TextView) findViewById(R.id.TextView05);
		speed = (TextView) findViewById(R.id.TextView06);
		accuracy = (TextView) findViewById(R.id.TextView07);
		providerS = (TextView) findViewById(R.id.TextView08);

		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
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

		mMap.addMarker(new MarkerOptions().position(new LatLng(43.0827, 25.6337)).title("Hello world"));
		mMap.addMarker(new MarkerOptions().position(new LatLng(10, 10)).title("Hello world"));

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
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_dumpLocations) {
			dumpLocations();
			return true;
		} else if (id == R.id.action_clearLocations) {
			((App) getApplication()).getModel().getLocationMaps().clear();
			return true;
		} else if (id == R.id.action_clearPointsMarker) {
			try {
				mMap.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} else if (id == R.id.action_fillDemoData) {

			fillDemoData();

			return true;
		}

		return super.onOptionsItemSelected(item);
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

			String date = new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong(string)));
			Log.w(MainActivity.class.toString(), "" + string + " " + date + " " + pointsSize);
			Iterator it = (Iterator) ((App) getApplication()).getModel().getLocationMaps().get(string).iterator();
			// int i = 0;
			// for (; it.hasNext();) {
			// Location location = ((MyLocation) it.next()).getLocatoin();
			// Log.w(MainActivity.class.toString(), "   " + i++ + " " + location.getLatitude() + " " + location.getLongitude());
			// }
		}
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
		time.setText(String.format("%s", new SimpleDateFormat("MM/dd HH:mm:ss.SSS").format(new Date(location.getTime()))));
		speed.setText(String.format("%.4f", location.getSpeed() * 3.6));// ms over ground ms*3.6 = kmh
		accuracy.setText(String.format("%.4f", location.getAccuracy()));
		providerS.setText(String.format("%s", location.getProvider()));

		try {
			elements.setText("Elements: " + ((App) getApplication()).getModel().getLocations().size() + location.getExtras().toString());
		} catch (Exception e) {
		}

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try {
					if (mMap == null)
						return;
					mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(
							"" + ((App) getApplication()).getModel().getLocations().size()));
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

	public void requestUpdates() {
		locationManager.requestLocationUpdates(provider, 200, 1, this);
		findViewById(R.id.table).setBackgroundColor(Color.GREEN);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		// removeUpdates();
	}

	public void removeUpdates() {
		locationManager.removeUpdates(this);
		findViewById(R.id.table).setBackgroundColor(Color.LTGRAY);
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
			String date = new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date(Long.parseLong(string)));
			aDisplay.add(" " + date + " " + pointsSize);
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
							int points = ((ArrayList) ((App) getApplication()).getModel().getLocationMaps().get(a.get(which))).size();
							Log.w(this.toString(), "loading: " + points + " points");
							int i = 0;
							for (; iterator.hasNext();) {
								Location location = ((MyLocation) iterator.next()).getLocatoin();
								mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("" + i++));
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

		String key = "" + System.currentTimeMillis();
		int points = ((App) getApplication()).getModel().getLocations().size();

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
			Toast.makeText(this, "Problem in writing points!", Toast.LENGTH_SHORT).show();
		}

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

		Toast.makeText(this, "Saved " + points + " points!", Toast.LENGTH_SHORT).show();
	}

	public void reqUp(View v) {
		requestUpdates();
	}

	public void remUp(View v) {
		removeUpdates();
	}

}
