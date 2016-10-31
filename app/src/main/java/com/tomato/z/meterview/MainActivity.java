package com.tomato.z.meterview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{
    MeterView meterView;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meterView = (MeterView) findViewById(R.id.meterView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
//        thread.start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        meterView.setPercent(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    int progress = (int) (msg.arg1 * 1.0f / msg.arg2 * 100);
                    meterView.setPercent(progress);
                    seekBar.setProgress(progress);
                    break;
            }
        }
    };

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            String fileUrl = "http://111.7.174.149/cache/gdown.baidu.com/data/wisegame/81cce5bbe49b4301/QQ_422.apk?ich_args2=238-31152314040098_5008fd23eaccae3ec6cea9c371a15606_10068001_9c88632bdfcaf9d49e33518939a83798_b29e13b2a160d232c8c532dce097b7a8";
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStream inputStream = conn.getInputStream();
                int curLen = -1;
                byte[] bytes = new byte[1024];
                int totalBytes = conn.getContentLength();
                int curTotalBytes = 0;

                while((curLen = inputStream.read(bytes)) != -1){
                    curTotalBytes += curLen;
                    //在此写入本地文件即可，本次省略，即只下载，不保存。
                    Message msg = handler.obtainMessage();
                    msg.arg1 = curTotalBytes;
                    msg.arg2 = totalBytes;
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                inputStream.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}
