package com.podevs.android.poAndroid.teambuilder;

import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.pokeinfo.StatsInfo;

public class EVSlider {
	public interface EVListener {
		public void onEVChanged(int stat, int ev);
	}

	private TextView total;
	private SeekBar slider;
	private TextView label;
	private EditText edit;
	public EVListener listener;
	
	
	EVSlider(View lay, final int stat) {
		total = (TextView)lay.findViewById(R.id.total);
		slider = (SeekBar)lay.findViewById(R.id.slider);
		label = (TextView)lay.findViewById(R.id.label);
		edit = (EditText)lay.findViewById(R.id.edit);
		label.setText(StatsInfo.Shortcut(stat));
		
		slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (!fromUser) {
					return;
				}
				
				/* Steps of 4 */
				int pr = (progress / 4) * 4;
				
				setNum(pr);
				if (listener != null) {
					listener.onEVChanged(stat, pr);
				}
			}
		});
	}
	
	void setNum(int num) {
		slider.setProgress(num);
		edit.setText(Integer.toString(num));
	}

	void setTotal(int num) {
		total.setText(Integer.toString(num));
	}
}
