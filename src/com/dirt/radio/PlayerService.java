package com.dirt.radio;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageButton;

public class PlayerService extends Service implements OnErrorListener,
		OnPreparedListener {

	public static final String MY_SERVICE = "com.dirt.radio.MY_SERVICE";
	public static MediaPlayer player;
	private Handler progressBarHandler = new Handler();;
	private Utilities utils;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		utils = new Utilities();

		super.onCreate();

	}

	// --------------onStartCommand-------------------------------------------------------------------------//
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("SERVICE", "onStartCommand()");
		try {
			playSong("http://psybernetics.org.uk:1337/");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		super.onStart(intent, startId);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}//

	@Override
	public void onPrepared(MediaPlayer play) {
		play.start();
		String BROADCAST_ACTION = "com.dirt.radio.broadcast";
		Intent broadcast = new Intent();
		Bundle b = new Bundle();
		b.putBoolean("on", true);
		broadcast.putExtras(b);
		broadcast.setAction(BROADCAST_ACTION);
		sendBroadcast(broadcast);
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {

		PlayerService.this.stopSelf();
		Log.d("ERROR", "ERROR");
		return false;

	}

	public static void playSong(String songIndex) {

		try {
			player.reset();
			player.setDataSource(songIndex);
			player.prepareAsync();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("Player Service", "Player Service Stopped");
		if (player != null) {
			if (player.isPlaying()) {
				player.stop();
			}
			player.release();
		}

		String BROADCAST_ACTION = "com.dirt.radio.broadcast";
		Intent broadcast = new Intent();
		Bundle b = new Bundle();
		b.putBoolean("on", false);
		broadcast.putExtras(b);
		broadcast.setAction(BROADCAST_ACTION);
		try {
			sendBroadcast(broadcast);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
