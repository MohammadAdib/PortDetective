package genius.mohammad.portdetective;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
	    setContentView(R.layout.mainlayout);
    	Button b = (Button)this.findViewById(R.id.startButton);
        b.setOnClickListener(this);
        String ip = getLocalIpAddress();
        if(ip != null) {
        	Toast.makeText(getApplicationContext(), "Local IP" + getNetworkType() + ": " + ip, Toast.LENGTH_LONG).show();
        }else{
        	Toast.makeText(getApplicationContext(), "No network connection detected.", Toast.LENGTH_LONG).show();
        }
        EditText ip1 = (EditText)findViewById(R.id.ip1);
        ip1.requestFocus();
        getSystemService(Context.INPUT_METHOD_SERVICE);
        addListeners();
    }
    
    public String getLocalIpAddress() {
        try {
        	WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
        	return Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
        } catch (Exception ex) {
        }
        return null;
    }
    
    public String getNetworkType() {
    	TelephonyManager teleMan =  (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);    	
	    int networkType = teleMan.getNetworkType();
	    WifiManager wifi = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	    boolean b=wifi.isWifiEnabled();
	    if (!b) {
	    	switch (networkType)
		    {
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
	    }else{
	    	return " (Wifi)";
	    }
	    return "";
    }

	public void onClick(View v) {
		String error = "Fatal Error Occured.";
		try {
			String ip;
			int port1 = 0, port2 = 65535;
			EditText ip1 = (EditText)findViewById(R.id.ip1);
			EditText ip2 = (EditText)findViewById(R.id.ip2);
			EditText ip3 = (EditText)findViewById(R.id.ip3);
			EditText ip4 = (EditText)findViewById(R.id.ip4);
			ip = ip1.getText() + "." + ip2.getText() + "." + ip3.getText() + "." + ip4.getText();
			EditText portText1 = (EditText)findViewById(R.id.port1);
			EditText portText2 = (EditText)findViewById(R.id.port2);
			//Sentinels
			if(ip1.getText().length() < 1 || ip2.getText().length() < 1 || ip3.getText().length() < 1 || ip4.getText().length() < 1 || portText1.getText().toString().length() < 1 || portText2.getText().toString().length() < 1) {
				error = "Invalid Entries. Please provide a valid IP Address and port range.";
				throw new Exception();
			}
			if(ip1.getText().length() > 3 || ip2.getText().length() > 3 || ip3.getText().length() > 3 || ip4.getText().length() > 3) {
				error = "Invalid IP Address.";
				throw new Exception();
			}
			try {
				port1 = Integer.parseInt(portText1.getText().toString());
				port2 = Integer.parseInt(portText2.getText().toString());
				if(port1 > 65535 || port1 < 1 || port2 > 65535 || port2 < 1) {
					throw new Exception();
				}
			}catch(Exception e) {
				error = "Invalid port entries. Ports may range from 1 to 65535.";
				throw new Exception();
			}
			if(port2 - port1 <= 0) {
				error = "Invalid port ranges.";
				throw new Exception();
			}
			if(getLocalIpAddress() != null) {
				RAM.ip = ip;
				RAM.port1 = port1;
				RAM.port2 = port2;
				Intent i = new Intent(this, PortScanningActivity.class);
				startActivity(i);
			}else{
	        	Toast.makeText(getApplicationContext(), "No network connection detected.", Toast.LENGTH_LONG).show();
	        	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
	        }
		}catch(Exception e) {
			Toast toast = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public void addListeners() {
		final EditText ip1 = (EditText)findViewById(R.id.ip1);
		final EditText ip2 = (EditText)findViewById(R.id.ip2);
		final EditText ip3 = (EditText)findViewById(R.id.ip3);
		final EditText ip4 = (EditText)findViewById(R.id.ip4);
		final EditText portText1 = (EditText)findViewById(R.id.port1);
		final EditText portText2 = (EditText)findViewById(R.id.port2);
    	final Button b = (Button)this.findViewById(R.id.startButton);
    	
        ip1.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 2) {
                	ip2.requestFocus();
                }
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 3) {                
                	ip1.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
        ip2.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 2) {
                	ip3.requestFocus();
                }
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 3) {                
                	ip2.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
        ip3.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 2) {
                	ip4.requestFocus();
                }
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 3) {                
                	ip3.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
        ip4.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 2) {
                	portText1.requestFocus();
                }
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 3) {                
                	ip4.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
        portText1.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 4) {
                	portText2.requestFocus();
                }
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 5) {                
                	portText1.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
        portText2.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 4) {                
                	b.requestFocus();
                }
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 5) {                
                	portText2.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
        
        ip2.setOnKeyListener(new OnKeyListener() {           
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if(ip2.getText().toString().length() < 1) {;
                    	ip1.requestFocus();
                    }
                }
                return false;
            }
        });
        
        ip3.setOnKeyListener(new OnKeyListener() {           
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if(ip3.getText().toString().length() < 1) {;
                    	ip2.requestFocus();
                    }
                }
                return false;
            }
        });
        
        ip4.setOnKeyListener(new OnKeyListener() {           
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if(ip4.getText().toString().length() < 1) {;
                    	ip3.requestFocus();
                    }
                }
                return false;
            }
        });
        
        portText1.setOnKeyListener(new OnKeyListener() {           
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if(portText1.getText().toString().length() < 1) {;
                    	ip4.requestFocus();
                    }
                }
                return false;
            }
        });
        
        portText2.setOnKeyListener(new OnKeyListener() {           
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if(portText2.getText().toString().length() < 1) {;
                    	portText1.requestFocus();
                    }
                }
                return false;
            }
        });
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
	            if(ip != null) {
	            	Toast.makeText(getApplicationContext(), "Local IP" + getNetworkType() + ": " + ip, Toast.LENGTH_LONG).show();
	            }else{
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
}