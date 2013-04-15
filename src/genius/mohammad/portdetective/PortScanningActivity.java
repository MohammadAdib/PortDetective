package genius.mohammad.portdetective;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PortScanningActivity extends Activity implements OnClickListener {
	Handler mHandler;
	PortScanner scanner;
	long startTime = System.currentTimeMillis();
	int lastPortChecked = 0;

	private Runnable mUpdate = new Runnable() {
		public void run() {
			TextView label = (TextView) findViewById(R.id.label);
			TextView console = (TextView) findViewById(R.id.portconsole);
			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
			if (scanner.running) {
				if (lastPortChecked != scanner.currentPort) {
					lastPortChecked = scanner.currentPort;
					startTime = System.currentTimeMillis();
				}
				// Add ... "
				int dots = (int) (((System.currentTimeMillis() - startTime) / 1000.0) + .5);
				String s = "";
				for (int i = 0; i < dots % 4; i++) {
					s += ".";
				}
				progressBar.setProgress(scanner.progress);
				console.setText(scanner.output);
				if (scanner.currentPort > -1) {
					label.setText(scanner.progress + "% Querying port " + scanner.currentPort + s);
				} else if (scanner.currentPort == -2) {
					label.setText("Resolving Host" + s);
					console.setText("Host " + RAM.ip + " cannot be reached.");
				}
				if (System.currentTimeMillis() - startTime > 7000 && lastPortChecked == scanner.currentPort) {
					scanner.timeOut();
					label.setText("Connection timed out.");
					progressBar.setProgress(100);
					startTime = System.currentTimeMillis();
				}
				// Repeat thread execution
				mHandler.post(this);
			} else if (scanner.currentPort == -1) {
				label.setText("100% Port Scan Completed.");
				console.setText(scanner.output);
				progressBar.setProgress(100);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		scanner = new PortScanner(PortScanningActivity.this.getApplicationContext().getResources().openRawResource(R.raw.ports));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.portchechinglayout);
		start();
		mHandler = new Handler();
		mHandler.post(mUpdate);
		Button b = (Button) findViewById(R.id.exportButton);
		b.setOnClickListener(this);
	}

	private void start() {
		// Initialize TextView console
		scanner = new PortScanner(this.getApplicationContext().getResources().openRawResource(R.raw.ports));
		scanner.scan();
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception ex) {
		}
		return null;
	}

	public String getNetworkType() {
		TelephonyManager teleMan = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = teleMan.getNetworkType();
		WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		boolean b = wifi.isWifiEnabled();
		if (!b) {
			switch (networkType) {
			case 7:
				return " (1xRTT)";
			case 4:
				return " (CDMA)";
			case 2:
				return " (EDGE)";
			case 14:
				return " (eHRPD)";
			case 5:
				return " (EVDO rev. 0)";
			case 6:
				return " (EVDO rev. A)";
			case 12:
				return " (EVDO rev. B)";
			case 1:
				return " (GPRS)";
			case 8:
				return " (HSDPA)";
			case 10:
				return " (HSPA)";
			case 15:
				return " (HSPA+)";
			case 9:
				return " (HSUPA)";
			case 11:
				return " (iDen)";
			case 13:
				return " (LTE)";
			case 3:
				return " (UMTS)";
			}
		} else {
			return " (Wifi)";
		}
		return "";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.showIPMenuItem:
			String ip = getLocalIpAddress();
			if (ip != null) {
				Toast.makeText(getApplicationContext(), "Local IP" + getNetworkType() + ": " + ip, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "No network connection detected.", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.lookUpPortMenuItem:
			Intent i = new Intent(this, PortDescriptorActivity.class);
			startActivity(i);
			break;
		case R.id.aboutMenuItem:
			Intent i2 = new Intent(this, About.class);
			startActivity(i2);
			break;
		}
		return true;
	}

	@SuppressLint("SimpleDateFormat")
	public void onClick(View v) {
		try {
			if (scanner.running == false) {
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File(sdCard.getAbsolutePath() + "/Port Detective/");
				dir.mkdir();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
				Date date = new Date();
				File file = new File(dir, RAM.ip + "@" + dateFormat.format(date) + ".txt");
				FileOutputStream f = new FileOutputStream(file);
				byte[] data = new String("Host " + RAM.ip + " Time:" + dateFormat.format(date) + "\n\n" + scanner.output.trim()).getBytes();
				f.write(data);
				f.flush();
				f.close();
				Toast.makeText(getApplicationContext(), "Saved log to: " + dir + "//" + RAM.ip + "@" + dateFormat.format(date) + ".txt", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "Scanning not yet finished.", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Error occured while saving log.", Toast.LENGTH_LONG).show();
			Log.d("IO", e.getMessage());
		}
	}
}