package com.chig.gameq3;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        gameSurface.setXAccel((int) event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }



    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap myImage;
        Bitmap myImage2;
        Bitmap enemy;
        Paint paintProperty;
        int xAccel;

        int screenWidth;
        int screenHeight;

        int score = 0;

        boolean hit = false;
        boolean speedUp = false;

        public GameSurface(Context context) {
            super(context);

            holder=getHolder();

            myImage = BitmapFactory.decodeResource(getResources(),R.drawable.ship);
            myImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.brokenship);
            enemy = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(100);

            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    speedUp = !speedUp;

                    return false;
                }
            });

        }

        @Override
        public void run() {
            int value = 5;
            int enemyXVal = (int)(Math.random() * (screenWidth - 100)) + 100;
            int enemyYVal = 0;
            while (running){
                Log.d("UGH", running + "");
                if (!holder.getSurface().isValid())
                    continue;

                Canvas canvas= holder.lockCanvas();
                canvas.drawRGB(0, 0, 255);

                if((Integer) xAccel != null){
                    if(xAccel > 0){
                        value-= (xAccel * 5);
                    }
                    else if(xAccel < 0){
                        value-= (xAccel * 5);
                    }
                    else{

                    }
                }

                canvas.drawBitmap(myImage,100+value, 2000, null);
                canvas.drawBitmap(enemy, enemyXVal, enemyYVal, null);

                Rect hitBox = new Rect(100 + value, 2000, 100 + value + myImage.getWidth(), 2000 + myImage.getHeight());
                Rect enemyHitBox = new Rect(enemyXVal, enemyYVal, enemyXVal + enemy.getWidth(), enemyYVal + enemy.getHeight());
               // canvas.drawRect(hitBox, new Paint());
                canvas.drawRect(enemyHitBox, new Paint());
                Paint text = new Paint();
                text.setTextSize(50.0f);
                canvas.drawText("Score: " + score, 50.0f, 50.0f, text);
                if(!speedUp){
                    enemyYVal += 25;
                }
                else if(speedUp){
                    enemyYVal += 50;
                }


                if(enemyYVal >= screenHeight){
                    enemyYVal = 0;
                    enemyXVal = (int)(Math.random() * (screenWidth - 100)) + 100;
                    score++;
                }

                if(enemyHitBox.intersect(hitBox)){
                    canvas.drawBitmap(myImage2,100+value, 2000, null);


                    //INSERT DELAY HERE


                    enemyYVal = 0;
                    enemyXVal = (int)(Math.random() * (screenWidth - 100)) + 100;

                }



                holder.unlockCanvasAndPost(canvas);
            }//while running
        }//run

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

        public void setXAccel(int xAccel){
            this.xAccel = xAccel;
        }


    }//GameSurface
}//Activity