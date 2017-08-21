package com.podevs.android.poAndroid.teambuilder;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.pokeinfo.StatsInfo;

public class EVSlider {
	public interface EVListener {
		void onEVChanged(int stat, int ev);
	}

	private TextView total;
	private SeekBar slider;
	private EditText edit;
	public EVListener listener;
	private View parent;
	
	EVSlider(View lay, final int stat) {
		total = (TextView)lay.findViewById(R.id.total);
		slider = (SeekBar)lay.findViewById(R.id.slider);
		TextView label = (TextView) lay.findViewById(R.id.label);
		edit = (EditText)lay.findViewById(R.id.edit);
		edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
		label.setText(StatsInfo.ShortcutRes(stat));
		parent = lay;

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

		edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					if (listener != null) {
						listener.onEVChanged(stat, Integer.parseInt(edit.getText().toString()));
					}
					return true;
				}
				return false;
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

	void setVisibility(int visibility) {
		parent.setVisibility(visibility);
	}
}
