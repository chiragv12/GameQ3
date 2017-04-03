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
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    CountDownTimer timer;
    int time = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);

        timer = new CountDownTimer(31000, 1000){
            @Override
            public void onTick(long l) {
                time--;
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
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
        Paint textGameOver;
        int xAccel;

        int screenWidth;
        int screenHeight;

        int score = 0;

        boolean hit = false;
        int hitCount = 0;
        boolean speedUp = false;
        boolean gameOver = false;
        long start = System.currentTimeMillis();

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

            textGameOver = new Paint();
            textGameOver.setTextSize(100.0f);

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
                //Log.d("UGH", running + "");
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

                if(!hit) {
                    canvas.drawBitmap(myImage, 100 + value, 2000, null);
                }else if(hit){
                    canvas.drawBitmap(myImage2,100+value, 2000, null);
                    hitCount++;
                    if(hitCount == 100){
                        hit = false;
                    }
                }
                canvas.drawBitmap(enemy, enemyXVal, enemyYVal, null);

                Rect hitBox = new Rect(100 + value, 2000, 100 + value + myImage.getWidth(), 2000 + myImage.getHeight());
                Rect enemyHitBox = new Rect(enemyXVal, enemyYVal, enemyXVal + enemy.getWidth(), enemyYVal + enemy.getHeight());
                //canvas.drawRect(hitBox, new Paint());
                canvas.drawRect(enemyHitBox, new Paint());
                Paint text = new Paint();
                text.setTextSize(100.0f);
                canvas.drawText("Score: " + score, 50.0f, 80.0f, text);
                canvas.drawText("Time: " + time, 50.0f, 200.0f, text);
                if(100 + value <= 0){
                    value = -100;
                    canvas.drawBitmap(myImage, 0, 2000, null);
                    hitBox = new Rect(0, 2000, 0 + myImage.getWidth(), 2000 + myImage.getHeight());
                }

                if((100 + value) >= screenWidth - myImage.getWidth()){
                    value = screenWidth - myImage.getWidth() - 100;
                    canvas.drawBitmap(myImage, screenWidth - 50, 2000, null);
                    hitBox = new Rect(screenWidth - 50, 2000, screenWidth, 2000 + myImage.getHeight());
                }


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
                    hit = true;
                    hitCount = 0;
                    enemyYVal = 0;
                    enemyXVal = (int)(Math.random() * (screenWidth - 100)) + 100;


                }

                if(time == 0){
                    running = false;
                    canvas.drawText("GAME OVER, SCORE: " + score, 150, screenHeight / 2, textGameOver);
                }
                Log.d("LOL", time + "");




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