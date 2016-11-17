package astromech.r2d2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class JoystickActivity extends Activity implements View.OnTouchListener {

    CurrentView v;
    Bitmap JoystickBack;
    Bitmap Joystick;
    float x, y;
    long xMap, yMap, xSize, ySize;

    long map(long x, long in_min, long in_max, long out_min, long out_max) {
        return (((x - in_min) * (out_max - out_min)) / (in_max - in_min)) + out_min;
    }

    protected void onStart() {
        super.onStart();
        BluetoothSPP bt;
        bt = new BluetoothSPP(this);
        if (!bt.isBluetoothEnabled()) {
            // Do something if bluetooth is disabled
            Toast.makeText(this,
                    "Even though i am fluent i over 6 million languages, i need bluetooth enabled on this device", Toast.LENGTH_SHORT).show();
        } else {
            // Do something if bluetooth is already enabled
            Toast.makeText(this,
                    "Bluetooth is enabled :)", Toast.LENGTH_LONG).show();
            bt.startService(BluetoothState.DEVICE_OTHER);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        v = new CurrentView(this);
        v.setOnTouchListener(this);

        JoystickBack = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Joystick = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        x = size.x / 2;
        y = size.y / 2;
        xMap = size.x / 2;
        yMap = size.y / 2;
        xSize = size.x;
        ySize = size.y;

        setContentView(v);
    }

    @Override
    protected void onPause() {
        super.onPause();
        v.pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        v.resume();
    }

    @Override
    public boolean onTouch(View v, MotionEvent me) {
        switch (me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = me.getX();
                y = me.getY();
                xMap = map((long) x, 0, xSize, 0, 255);
                yMap = map((long) y, 0, ySize, 0, 255);
                break;
            case MotionEvent.ACTION_UP:
                x = xSize / 2;
                y = ySize / 2;
                xMap = map((long) x, 0, xSize, 0, 255);
                yMap = map((long) y, 0, ySize, 0, 255);
                break;
            case MotionEvent.ACTION_MOVE:
                x = me.getX();
                y = me.getY();
                xMap = map((long) x, 0, xSize, 0, 255);
                yMap = map((long) y, 0, ySize, 0, 255);
                break;
        }
        Log.v("COORDS", "(X:" + x + "/" + xMap + ") (Y:" + y + "/" + yMap + ")");
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    protected class CurrentView extends SurfaceView implements Runnable {

        private boolean threadIsRunning = false;
        private Thread _thread;
        private SurfaceHolder _holder;

        public CurrentView(Context context) {
            super(context);
            _holder = getHolder();
        }

        @Override
        public void run() {
            while (threadIsRunning) {

                if (!_holder.getSurface().isValid()) {
                    continue;
                }
                Canvas c = _holder.lockCanvas();
                c.drawARGB(255, 0, 0, 0);
                c.drawBitmap(Joystick, x - (Joystick.getWidth() / 2), y - (Joystick.getHeight() / 2), null);
                _holder.unlockCanvasAndPost(c);
            }
        }

        public void pause() {
            threadIsRunning = false;
            try {
                _thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _thread = null;
        }

        public void resume() {
            threadIsRunning = true;
            _thread = new Thread(this);
            _thread.start();
        }
    }
}

