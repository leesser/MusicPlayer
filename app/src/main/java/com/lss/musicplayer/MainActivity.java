package com.lss.musicplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private MyService.MyBinder myBinder;
    private MyConn mConn;
    private Intent mIntent;
    private SeekBar mSeekBar;
    private Thread mThread;
    private EditText et_fileName;

    //handler更新ui界面
    private Handler mHandle=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 10:
                    int progress= (int) msg.obj;
                    mSeekBar.setProgress(progress);
                    break;
                case 20:
                    int progresses= (int) msg.obj;
                    mSeekBar.setProgress(progresses);
                    myBinder.playProgress(progresses);
                    break;
            }
        }
    };
    //初始化控件,开启服务
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConn = new MyConn();
        et_fileName = (EditText) findViewById(R.id.et_path);
        findViewById(R.id.bt_play).setOnClickListener(this);
        findViewById(R.id.bt_pause).setOnClickListener(this);
        findViewById(R.id.bt_replay).setOnClickListener(this);
        findViewById(R.id.bt_stop).setOnClickListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Message msg1 = Message.obtain();
                int progress = seekBar.getProgress();
                msg1.obj=progress;
                msg1.what=20;
                mHandle.sendMessage(msg1);
            }
        });
        mIntent = new Intent(this, MyService.class);
        bindService(mIntent, mConn,BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        File SDpath = Environment.getExternalStorageDirectory();
        String fileName = et_fileName.getText().toString().trim();
        switch (v.getId()){
            case R.id.bt_play:
                File file = new File(SDpath.getPath(),fileName);
                String path=file.getPath();
                if (file.exists()&&file.length()>0) {
                    myBinder.plays(path);
                    initSeekBar();
                    updateProgress();
                }else {
                    Toast.makeText(this,"文件找不到",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.bt_pause:
                myBinder.pauses();
                break;

            case R.id.bt_replay:
                myBinder.repalys();
                break;

            case R.id.bt_stop:
                //停止音乐之前首先要退出子线程
                mThread.interrupt();
                if (mThread.isInterrupted()) {
                    myBinder.stops();
                }
                break;
        }
    }
    //初始化进度条的长度,获取音乐文件的长度
    private void initSeekBar(){
        int musicwidth = myBinder.getMusicwidth();
        mSeekBar.setMax(musicwidth);
    }

    //更新进度条的进度
    private void updateProgress(){
        mThread=new Thread(){
            @Override
            public void run() {
                super.run();
                while (!interrupted()) {
                    int position = myBinder.getCurrentPositions();
                    Message msg = Message.obtain();
                    msg.obj=position;
                    msg.what=10;
                    mHandle.sendMessage(msg);
                }
            }
        };
        mThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果线程没有退出,则退出
        if (mThread!=null&&mThread.isInterrupted()){
            mThread.interrupt();
        }
        unbindService(mConn);
    }

    private class MyConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder= (MyService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
