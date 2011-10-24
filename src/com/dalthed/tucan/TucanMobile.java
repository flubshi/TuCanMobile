package com.dalthed.tucan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class TucanMobile extends Application {
	private static Context Appcontext;
	public final static String TUCAN_HOST = "www.tucan.tu-darmstadt.de";
	public final static String TUCAN_PROT = "https://";

	@Override
	public void onCreate() {
		TucanMobile.Appcontext = getApplicationContext();
	}

	public static Context getAppContext() {
		return Appcontext;
	}

	public void generateNoteOnSD(String sFileName, String sBody) {
		try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"Notes");
			if (!root.exists()) {
				root.mkdirs();
			}
			File gpxfile = new File(root, sFileName);
			FileWriter writer = new FileWriter(gpxfile);
			writer.append(sBody);
			writer.flush();
			writer.close();
			Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
			String importError = e.getMessage();
			Log.e("TuCanMobile", importError);
		}
	}

}
