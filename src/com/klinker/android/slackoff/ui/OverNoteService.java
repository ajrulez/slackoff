package com.klinker.android.slackoff.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.klinker.android.slackoff.R;

public class OverNoteService extends Service {

    // just a random number for the id
    public final int FOREGROUND_SERVICE_ID = 2532;

    // some of the basic info i will use
    private Context mContext;
    private SharedPreferences sharedPrefs;
    private Vibrator v;
    private Display d;
    private DisplayMetrics displayMatrix;
    private int height;
    private int width;

    // detects the gestures so i know what the user is doing
    private GestureDetector mGestureDetector;

    private WindowManager.LayoutParams noteParams;
    private WindowManager noteWindow;
    private View noteView;

    private EditText name;
    private EditText content;
    private Button save;
    private Button discard;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("over_note", "service started");

        mContext = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        // gets the display
        d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        displayMatrix = getResources().getDisplayMetrics();

        // registers the intentfilter to kill the service when the class has ended
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.STOP_NOTES");
        registerReceiver(stopNotes, filter);

        Notification notification = new Notification(R.drawable.ic_launcher, getResources().getString(R.string.app_name),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getResources().getString(R.string.app_name),
                "Click to open", pendingIntent);

        // because ice cream sandwhich doesn't support this
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            notification.priority = Notification.PRIORITY_MIN;
        }

        // creates a notification so the system knows not to kill the service
        startForeground(FOREGROUND_SERVICE_ID, notification);

        // initializes the vibrator service so i can use it
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // more setup stuff for the view
        initialSetup(height, width);
        setUpTouchListeners(height, width);
    }

    public void setUpTouchListeners(final int height, final int width) {
        noteView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                //mGestureDetector.onTouchEvent(event);

                if (touchedNoteHandle(event)) {
                    final int type = event.getActionMasked();

                    switch (type) {
                        case MotionEvent.ACTION_DOWN:

                            // Vibrate
                            v.vibrate(200);

                            return true;

                        case MotionEvent.ACTION_MOVE:

                            // update my view and where it is at

                            return true;

                        case MotionEvent.ACTION_UP:

                            // set the view

                            return true;
                    }
                }

                return false;
            }
        });
    }

    public boolean touchedNoteHandle(MotionEvent event) {
        return event.getX() > noteView.getX() && event.getX() < noteView.getX() + toDP(15)
                && event.getY() > noteView.getY() && event.getY() < noteView.getY() + toDP(height * .75);
    }

    public void initialSetup(int height, int width) {
        // creates the note from the resource file
        noteView = View.inflate(this, R.layout.over_note, null);

        // sets it up on the screen. it will start at the edge and the user will be able to swipe it out
        noteParams = new WindowManager.LayoutParams(
                (int) (width * .75),          // width of the note box
                (int) (height* .55),           // height of the note box
                width - 30,           // 15 density pixels shown on on the right side of the screen
                (int) (height * .2),        // starts 12.5% down the screen
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        noteParams.gravity = Gravity.TOP | Gravity.LEFT;
        noteParams.windowAnimations = android.R.style.Animation_InputMethod;

        // gets the system service
        noteWindow = (WindowManager) getSystemService(WINDOW_SERVICE);

        // sets up the attributes of the note
        name = (EditText) noteView.findViewById(R.id.name);
        content = (EditText) noteView.findViewById(R.id.content);
        discard = (Button) noteView.findViewById(R.id.discard);
        save = (Button) noteView.findViewById(R.id.save);

        noteWindow.addView(noteView, noteParams);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public BroadcastReceiver stopNotes = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // kills the notification, stops the service, then cleans up and clears the receiver
            stopForeground(true);
            stopSelf();
            unregisterReceiver(this);
        }
    };

    public int toDP(double px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) px, displayMatrix);
    }
}
