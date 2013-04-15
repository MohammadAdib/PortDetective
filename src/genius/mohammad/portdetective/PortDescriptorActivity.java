package genius.mohammad.portdetective;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PortDescriptorActivity extends Activity implements OnClickListener{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.portdescriptor);
		Button b = (Button)findViewById(R.id.portDescriptorButton);
		b.setOnClickListener(this);
		final EditText et = (EditText)findViewById(R.id.portTextBox);
		et.addTextChangedListener((new TextWatcher() {
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                
            }
			public void afterTextChanged(Editable s) {
				if(s.length() > 5) {                
                	et.setText(s.toString().substring(0, s.length() - 1));
                }
			}
        }));
		TextView lookUp = (TextView)findViewById(R.id.lookUp);
		lookUp.setOnClickListener(this);
		lookUp.setVisibility(View.INVISIBLE);
	}

	public void onClick(View v) {
		if (v == findViewById(R.id.portDescriptorButton)) {
			try {
				EditText et = (EditText)findViewById(R.id.portTextBox);
				PortDescriptor pd = new PortDescriptor(this.getApplicationContext().getResources().openRawResource(R.raw.ports));
				String desc = pd.describePort(Integer.parseInt(et.getText().toString()));
				if(Integer.parseInt(et.getText().toString()) < 49152) {
					TextView console = (TextView)findViewById(R.id.descriptionConsole);
					console.setText("\nPort " + Integer.parseInt(et.getText().toString()) + ":\n" + desc+"\n");
					TextView lookUp = (TextView)findViewById(R.id.lookUp);
					lookUp.setVisibility(View.VISIBLE);
					lookUp.setText("Look up port " + et.getText().toString() + " on the web.");
				}else{
					Toast.makeText(this.getApplicationContext(), "Invalid entry. Ports may only range from 0 to 49151.", Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e) {
				Toast.makeText(this.getApplicationContext(), "Error occured while trying to parse port number. Please try again!", Toast.LENGTH_SHORT).show();
			}
		}else if (v == findViewById(R.id.lookUp)) {
			EditText et = (EditText)findViewById(R.id.portTextBox);
			String url = "http://www.speedguide.net/port.php?port=" + et.getText().toString();
        	Intent i = new Intent(Intent.ACTION_VIEW);
        	i.setData(Uri.parse(url));  
        	startActivity(i);
		}
	}	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu2, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.webItem:
	        	String url = "http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml";  
	        	Intent i = new Intent(Intent.ACTION_VIEW);
	        	i.setData(Uri.parse(url));  
	        	startActivity(i);
	        	break;
	    }
	    return true;
	}
}
