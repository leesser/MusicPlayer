package com.lss.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by shuai on 16-7-18.
 */
public class MyService extends Service {

    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
    }

    public class MyBinder extends Binder {
        public void plays(String path) {
            play(path);
        }

        public void pauses() {
            pause();
        }

        public void repalys() {
            replay();
        }

        public void stops() {
            stop();
        }

        public int getMusicwidth() {
            return getMusicLength();
        }

        public int getCurrentPositions() {
            return getCurrentProgress();
        }

        public void playProgress(int progress) {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(progress);
                mMediaPlayer.start();
            }
        }

        //播放
        private void play(String path) {
            try {
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                    //指定参数为音频文件
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    //设置播放的音频路径
                    mMediaPlayer.setDataSource(path);
                    mMediaPlayer.prepare();
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                } else if (!mMediaPlayer.isPlaying()){
                    int progress = getCurrentProgress();
                    //设置文件从当前进度播放
                    mMediaPlayer.seekTo(progress);
                    try {
                        mMediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //暂停
        private void pause() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else if (mMediaPlayer != null && !(mMediaPlayer.isPlaying())) {
                mMediaPlayer.start();
            }
        }

        //重新播放
        // TODO: 16-7-18
        private void replay() {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(0);
                try {
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                mMediaPlayer.start();
            }
        }

        //停止
        private void stop() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } else {
                Toast.makeText(getApplicationContext(), "已停止", Toast.LENGTH_SHORT).show();
            }
        }

        //音乐文件的长度
        private int getMusicLength() {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getDuration();
            }
            return 0;
        }

        //当前进度
        // TODO: 16-7-18
        private int getCurrentProgress() {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getCurrentPosition();
            }
            return 0;
        }
    }
}
