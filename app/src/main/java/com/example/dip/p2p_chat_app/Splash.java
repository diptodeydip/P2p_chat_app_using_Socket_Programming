package com.example.dip.p2p_chat_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Animation myanim = AnimationUtils.loadAnimation(this,R.anim.mytransition);
        findViewById(R.id.splashtxt).startAnimation(myanim);
        findViewById(R.id.splashtxt1).startAnimation(myanim);
        findViewById(R.id.splashiv).startAnimation(myanim);

        Thread mythread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                }
                catch (InterruptedException e){}
                finally {
                    startActivity(new Intent(Splash.this,MainActivity.class));
                    finish();
                }
            }
        };

        mythread.start();
    }
}

