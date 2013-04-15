package genius.mohammad.portdetective;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class About extends Activity implements OnTouchListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}
	
	boolean touched;

	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touched = true;
			break;
		case MotionEvent.ACTION_UP:
			if(touched)
			finish();
			break;
		}
		return true;
	}
}