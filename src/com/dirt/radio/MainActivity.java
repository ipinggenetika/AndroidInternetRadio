package com.dirt.radio;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;

public class MainActivity extends Activity {

	public static String BROADCAST_ACTION = "com.dirt.radio.broadcast";
	LinearLayout ll;
	Button streamOne, streamTwo;
	WebView ib;
	ListView lv;
	ArrayList<Stations> SO;
	public static String stream = "";
	private VisualizerView mVisualizerView;
	private Visualizer mVisualizer;
	private LinearLayout mLinearLayout;
	public ProgressDialog pd;

	RadioButton radioswitch;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				pd.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (intent.getBooleanExtra("on", false)) {
				setupVisualizerFxAndUI();
			} else {
				radioswitch.setChecked(false);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mLinearLayout = (LinearLayout) findViewById(R.id.wave);
		mLinearLayout.setSoundEffectsEnabled(false);

		ib = (WebView) findViewById(R.id.img);
		ib.getSettings();
		ib.setBackgroundColor(0x00000000);
		// ib.setImageResource(R.drawable.logo);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//new GetImage(ib).execute();
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (PlayerService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	class GetImage extends AsyncTask<String, Void, String> {
		WebView iv;
		Bitmap myBitmap;

		public GetImage(WebView ibParam) {
			iv = ibParam;
		}

		@Override
		protected void onPreExecute() {

		}//

		@Override
		protected String doInBackground(String... params) {

			Document doc = null;
			try {
				doc = Jsoup.parse(new URL(
						"http://www.unityradio.fm/index.php?page_id=304"),
						10000);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Element img = doc.select("div#timetableContainer img").get(1);
			String imgSrc = img.attr("src");
			System.out.println("Img source: " + imgSrc);
			Log.d("url", imgSrc);
			Log.d("url", imgSrc);
			Log.d("url", imgSrc);
			Log.d("url", imgSrc);
			try {
				URL url = new URL(imgSrc);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				myBitmap = BitmapFactory.decodeStream(input);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return imgSrc;
		}

		@Override
		protected void onPostExecute(String result) {
			try {

				iv.loadUrl(result);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	public void setupVisualizerFxAndUI() {
		mVisualizerView = new VisualizerView(this);
		mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		mLinearLayout.addView(mVisualizerView);
		mVisualizer = new Visualizer(PlayerService.player.getAudioSessionId());
		mVisualizer.setEnabled(false);
		mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		mVisualizer.setDataCaptureListener(
				new Visualizer.OnDataCaptureListener() {
					public void onWaveFormDataCapture(Visualizer visualizer,
							byte[] bytes, int samplingRate) {
						mVisualizerView.updateVisualizer(bytes);
					}

					public void onFftDataCapture(Visualizer visualizer,
							byte[] bytes, int samplingRate) {
					}
				}, Visualizer.getMaxCaptureRate() / 2, true, false);

		mVisualizer.setEnabled(true);
	}

	@Override
	public void onPause() {
		super.onPause();

		initNotification();
		this.unregisterReceiver(this.mBroadcastReceiver);
		try {
			mVisualizer.setEnabled(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("yo", "yo");
		editor.commit();

	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter();
		iff.addAction(BROADCAST_ACTION);

		this.registerReceiver(this.mBroadcastReceiver, iff);
		addListenerOnButton();
		if (isMyServiceRunning()) {
			setupVisualizerFxAndUI();
		}
		mNotificationManager.cancelAll();

		try {
			if (!mVisualizer.getEnabled()) {

				mVisualizer.setEnabled(true);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// --------------------Push Notification
	// Set up the notification ID
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;

	// Create Notification
	private void initNotification() {

		Notification note = new Notification(R.drawable.hash, "  d i r t",
				System.currentTimeMillis());
		PendingIntent i = PendingIntent.getActivity(this, 0, new Intent(this,
				MainActivity.class), Notification.FLAG_AUTO_CANCEL);

		note.setLatestEventInfo(this, "unity radio", "", i);
		// note.number = ++count;
		note.flags |= Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(0, note);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	class VisualizerView extends View {
		private byte[] mBytes;
		private float[] mPoints;
		private Rect mRect = new Rect();

		private Paint mForePaint = new Paint();

		public VisualizerView(Context context) {
			super(context);
			init();
		}

		private void init() {
			mBytes = null;

			mForePaint.setStrokeWidth(1f);
			mForePaint.setAntiAlias(true);
			// mForePaint.setColor(Color.rgb(0, 153, 255));
			mForePaint.setColor(Color.rgb(255, 255, 255));
		}

		public void updateVisualizer(byte[] bytes) {
			mBytes = bytes;
			invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (mBytes == null) {
				return;
			}

			if (mPoints == null || mPoints.length < mBytes.length * 4) {
				mPoints = new float[mBytes.length * 4];
			}

			mRect.set(0, 0, getWidth(), getHeight());

			for (int i = 0; i < mBytes.length - 1; i++) {
				mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
				mPoints[i * 4 + 1] = mRect.height() / 2
						+ ((byte) (mBytes[i] + 128)) * (mRect.height() / 2)
						/ 128;
				mPoints[i * 4 + 2] = mRect.width() * (i + 1)
						/ (mBytes.length - 1);
				mPoints[i * 4 + 3] = mRect.height() / 2
						+ ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2)
						/ 128;
			}

			canvas.drawLines(mPoints, mForePaint);
		}
	}

	public void addListenerOnButton() {

		radioswitch = (RadioButton) findViewById(R.id.radioswitch);

		radioswitch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!isMyServiceRunning()) {

					pd = new ProgressDialog(MainActivity.this);

					pd.setTitle("Unity Radio");
					pd.setMessage("connecting...");
					pd.setCancelable(true);
					pd.setIndeterminate(true);
					pd.show();

					Intent i = new Intent();
					i.setClassName("com.dirt.radio",
							"com.dirt.radio.PlayerService");

					startService(i);

				} else {
					// stop service

					Log.d("", "STOPPING SERVCIE");
					stopService(new Intent(PlayerService.MY_SERVICE));
					radioswitch.setChecked(false);
				}

			}

		});

		if (!isMyServiceRunning()) {
			radioswitch.setChecked(false);
		}

	}

}
