package com.tgithubc.view;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private CircleLoadingLayout mLoadingView;
    private Timer mTimer;
    private int mProgress;
    private boolean isLoading = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (mProgress <= 100) {
                        mLoadingView.setProgress(mProgress++);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoadingView = (CircleLoadingLayout) findViewById(R.id.loading_view);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLoading) {
                    mTimer.cancel();
                    mLoadingView.stopAnimation();
                } else {
                    mProgress = 0;
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandler.sendEmptyMessage(1);
                        }
                    }, 150, 100);
                    mLoadingView.startAnimator();
                }
                isLoading = !isLoading;
            }
        });
    }
}
