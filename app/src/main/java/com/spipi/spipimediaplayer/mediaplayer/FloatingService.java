package com.spipi.spipimediaplayer.mediaplayer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.spipi.spipimediaplayer.AppState;
import com.spipi.spipimediaplayer.MediaView;
import com.spipi.spipimediaplayer.R;

/**
 * Created by alexandre on 18/08/15.
 */
public class FloatingService extends Service implements AppState.OnForeGroundListener {
    public static final String ENABLE_FLOATING_PLAYER = "enable_floating_player";
    private WindowManager mWindowManager;
    private MediaView image;
    public static FloatingService instance = null;
    private boolean contains;

    public FloatingService(){
        super();
        instance = this;
    }
    public static AppState.OnForeGroundListener listener = new AppState.OnForeGroundListener() {
        @Override
        public void onForeGroundState(Context applicationContext, boolean foreground) {
            if(!foreground&&PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(ENABLE_FLOATING_PLAYER,false)) {
                applicationContext.startService(new Intent(applicationContext, FloatingService.class));
            }
        }
    };
    public void onCreate() {
        super.onCreate();

        LayoutInflater li = LayoutInflater.from(this);
        image = (MediaView) li.inflate(R.layout.layout_floating_player, null);
        image.setAlpha((float) 0.5);
        AppState.addOnForeGroundListener(this);
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);



    }
    @Override
    public void onStart(Intent intent, int startID){
        super.onStart(intent, startID);
        addFloatingView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void addFloatingView() {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(ENABLE_FLOATING_PLAYER,false)&&!AppState.isForeGround()) {
            final WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT);

            paramsF.gravity = Gravity.TOP | Gravity.LEFT;
            paramsF.x = 0;
            paramsF.y = 100;
            mWindowManager.addView(image, paramsF);
            contains = true;
            try {

                image.setOnTouchListener(new View.OnTouchListener() {
                    WindowManager.LayoutParams paramsT = paramsF;
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = paramsF.x;
                                initialY = paramsF.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                            case MotionEvent.ACTION_MOVE:
                                paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                                paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                                mWindowManager.updateViewLayout(v, paramsF);
                                break;
                        }
                        return false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    public void removeFloatingView() {
        if(contains)
        mWindowManager.removeViewImmediate(image);
        contains = false;
        stopSelf();
    }


    @Override
    public void onForeGroundState(Context applicationContext, boolean foreground) {
        if(foreground)
            removeFloatingView();
    }
}
