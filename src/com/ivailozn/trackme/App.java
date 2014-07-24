package com.ivailozn.trackme;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.location.Location;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

public class App extends Application {

	public final static boolean DEBUG = false;

	private static Model model;

	public Model getModel() {
		if (model == null) {
			model = new Model();
		}
		return model;
	}

	public void setModel(Model model) {
		App.model = model;
	}

	@Override
	public void onLowMemory() {
		Log.w(App.class.toString(), "onLowMemory");
		super.onLowMemory();
	}

	public class Model {

		private HashMap<String, String> data = new HashMap<String, String>();

		private ArrayList<MyLocation> locations = new ArrayList<MyLocation>();
		// 23453452435 - date of the saving, arraylist with locations
		private HashMap<String, ArrayList<MyLocation>> locationMaps = new HashMap<String, ArrayList<MyLocation>>();

		public Model() {
			Log.d(this.toString(), "App - new Model();");
		}

		public ArrayList<MyLocation> getLocations() {
			return locations;
		}

		public void setLocations(ArrayList<MyLocation> locations) {
			this.locations = locations;
		}

		public void setLocationMaps(HashMap<String, ArrayList<MyLocation>> locationMaps) {
			this.locationMaps = locationMaps;
		}

		public HashMap<String, ArrayList<MyLocation>> getLocationMaps() {
			return locationMaps;
		}
	}

	private Location location;

	// manages auto set the locale
	private Locale locale = null;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (locale != null) {
			newConfig.locale = locale;
			Locale.setDefault(locale);
			getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		}
	}

	public void updateLocale(String lang) {
		Configuration config = getBaseContext().getResources().getConfiguration();
		if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
			locale = new Locale(lang);
			Locale.setDefault(locale);
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	// web sign in with social networks
	public final static String EXTRA_AUTH_SOCIAL = "social";

	// sign in request code
	public final static int KEY_REQUEST_CODE_MOBILE_PASSPORT = 102;

	// questions list activity
	public final static int REQUEST_CODE_ACTIVITY_CATEGORY_LIST_MINIMAL = 1003;

	HashMap<String, String> dataChache = new HashMap<String, String>();
	HashMap<String, String> sizeChache = new HashMap<String, String>();

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(this.toString(), "app onCreate(");

		init();
	}

	private void init() {

		// readData
		if (model == null)
			model = new Model();
		readData();
	}

	public void exit() {
		// writeData
		// PersistentHelper.writeObj(getObjFile(), obj);
		writeData();
	}

	// Resurrect all the persistent data
	private void readData() {
		try {
			// use buffering
			InputStream file = new FileInputStream(getObjFile());
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				Object o = null;

				dataChache = (o = input.readObject()) != null ? (HashMap<String, String>) o : null;
				sizeChache = (o = input.readObject()) != null ? (HashMap<String, String>) o : null;

				getModel().setLocations((o = input.readObject()) != null ? (ArrayList<MyLocation>) o : null);
				getModel().setLocationMaps((o = input.readObject()) != null ? (HashMap<String, ArrayList<MyLocation>>) o : null);
				Log.d(this.toString(), "sizeCache: " + sizeChache.toString());
			} finally {
				input.close();
			}
		} catch (ClassNotFoundException ex) {
			System.err.println(ex);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	// write persistent data
	private void writeData() {
		try {
			OutputStream file = new FileOutputStream(getObjFile());
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(dataChache);
				output.writeObject(sizeChache);
				output.writeObject(getModel().getLocations());
				output.writeObject(getModel().getLocationMaps());
			} finally {
				output.close();
			}
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	public HashMap<String, String> getSizeChache() {
		return sizeChache;
	}

	public HashMap<String, String> getDataChache() {
		return dataChache;
	}

	private File getObjFile() {
		return new File(getFilesDir(), "last_state_01.txt");
	}

}
