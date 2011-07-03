package be.vbsteven.bmtodesk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class C2DM extends BroadcastReceiver {


	private static final String URL = "http://bookmarktodesktopdev.appspot.com/api/registerdeviceid";
	private static final String C2DM_EMAIL = "steven.v.bael@gmail.com";
	private String responseMessage = "";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent.getAction().equals(
				"com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals(
				"com.google.android.c2dm.intent.RECEIVE")) {
			handleMessage(context, intent);
		}
	}

	private void handleMessage(final Context context, final Intent intent) {
		Log.d("C2DM", "C2DM message received!");
		final String url = intent.getStringExtra("url");
		Bundle b = intent.getExtras();
		Log.d("C2DM", "C2DM data: url=" + url);

		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}

	private void handleRegistration(final Context context, final Intent intent) {
		final String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {
			// Registration failed, should try again later.
			Log.d("C2DM",
					"registration error: " + intent.getStringExtra("error"));
		} else if (intent.getStringExtra("unregistered") != null) {
			Log.d("C2DM", "unregistered");
			// unregistration done, new messages from the authorized sender will
			// be rejected
		} else if (registration != null) {
			sendC2DMRegistrationId(context, registration);
			Log.d("C2DM", "registered successfully with registration_id: "
					+ registration);
			// TODO do something with registration
		} else {
			Log.d("C2DM", "other registration error");
		}
		if (registration != null)
			sendC2DMRegistrationId(context, registration);
	}

	private void sendC2DMRegistrationId(Context context, String registration) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(URI.create(URL));

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("username", Global.getUsername(context)));
			nameValuePairs.add(new BasicNameValuePair("password", Global.getPassword(context)));
			nameValuePairs.add(new BasicNameValuePair("deviceid", registration));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpclient.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				Log.e("C2DM", line);
				line = reader.readLine();
			}
			responseMessage = reader.readLine();
		} catch (Exception e) {
			Log.e("C2DM", "error sending C2DM registration", e);
			responseMessage = "REQUESTFAILED";
		}

		Log.d("C2DM", "sendC2DMRegistrationId returned: " + responseMessage);
	}

	public static void registerToC2DM(final Context context) {
		Log.d("C2DM", "registering...");
		final Intent registrationIntent = new Intent(
				"com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context,
				0, new Intent(context, C2DM.class), 0)); // boilerplate
		registrationIntent.putExtra("sender", C2DM_EMAIL);
		context.startService(registrationIntent);
		Log.d("C2DM", "registration sent");
	}

}