package com.podevs.android.poAndroid.teambuilder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.podevs.android.poAndroid.R;


public class ColorPickerActivity extends Activity {
    private GLSurfaceView mGLSurfaceView;
    private SeekBar mValueBar;
    private EditText mR;
    private EditText mG;
    private EditText mB;
    public static float width = 2000;
    public static float height = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        int color = bundle.getInt("color", Color.BLACK);

        setContentView(R.layout.color_picker_layout);

        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);

        mGLSurfaceView.setEGLContextClientVersion(2);

        final ColorPickerRenderer renderer = new ColorPickerRenderer();

        mGLSurfaceView.setRenderer(renderer);

        ViewTreeObserver vto = mGLSurfaceView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                width = mGLSurfaceView.getWidth();
                height = mGLSurfaceView.getHeight();

                ViewTreeObserver obs = mGLSurfaceView.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        final TextView lazy = (TextView) findViewById(R.id.lazyColorView);
        mR = (EditText) findViewById(R.id.editTextR);
        mG = (EditText) findViewById(R.id.editTextG);
        mB = (EditText) findViewById(R.id.editTextB);

        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                float v = renderer.value;

                float h = x/width * 360f;
                float s = 1f - y/height;

                int color = Color.HSVToColor(new float[]{h, s, v});

                if (PickerUtils.isValidColor(color )) {
                    lazy.setBackgroundColor(color);
                    hold();
                    mR.setText(Integer.toString(PickerUtils.red(color)));
                    mG.setText(Integer.toString(PickerUtils.green(color)));
                    mB.setText(Integer.toString(PickerUtils.blue(color)));
                    release();
                } else {
                    lazy.setBackgroundColor(Color.BLACK);
                }
                return true;
            }
        });

        mValueBar = (SeekBar) findViewById(R.id.valueBar);

        mValueBar.setMax(255);
        mValueBar.setProgress(100);

        mValueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                renderer.value = progress/255f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (color == Color.BLACK) {
            color = Color.argb(255, 188, 27 ,27);
        }

        hold();
        mR.setText(Integer.toString(PickerUtils.red(color)));
        mG.setText(Integer.toString(PickerUtils.green(color)));
        mB.setText(Integer.toString(PickerUtils.blue(color)));
        release();

        float[] f = new float[3];
        Color.colorToHSV(color,f);
        mValueBar.setProgress((int) (f[2] * 255f));

        renderer.value = mValueBar.getProgress()/255f;

        lazy.setBackgroundColor(color);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (fromUser()) {
                    int color = colorFromEditTexts();
                    if (PickerUtils.isValidColor(color)) {
                        lazy.setBackgroundColor(color);
                    }
                    float[] f = new float[3];
                    Color.colorToHSV(color,f);
                    mValueBar.setProgress((int) (f[2] * 255f));
                }
            }
        };

        mR.addTextChangedListener(textWatcher);
        mB.addTextChangedListener(textWatcher);
        mG.addTextChangedListener(textWatcher);

        final Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("color", colorFromEditTexts());
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        final Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            }
        });
    }

    private int colorFromEditTexts() {
        String s = mR.getText().toString();
        int r = Integer.parseInt(s.equals("") ? "0" : s);
        s = mG.getText().toString();
        int g = Integer.parseInt(s.equals("") ? "0" : s);
        s = mB.getText().toString();
        int b = Integer.parseInt(s.equals("") ? "0" : s);
        return Color.argb(255, r, g, b);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private boolean isUserEvent;
    public boolean fromUser() {
        return isUserEvent;
    }

    public void hold() {
        isUserEvent = false;
    }

    public void release() {
        isUserEvent = true;
    }
}
