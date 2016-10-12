package com.example.eason.micplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AudioManager am = null;
    private AudioRecord record = null;
    private AudioTrack track = null;
    private int sampleRate = 8000;
    private AudioThread thread = null;
    private TextView textView = null;

    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.info);
        init();
        thread = new AudioThread(this);
        thread.start();
    }

    @Override
    protected void onDestroy() {
        thread.stopRunning();
        super.onDestroy();
    }

    private void init() {
        getValidSampleRates();

        int min = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, min);

        record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);

//        final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buf_sz);


        int maxJitter = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
    }

    private void getValidSampleRates() {
        for (int rate : new int[] {/*8000, 11025, 16000, 22050, */44100}) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                sampleRate = rate;
                Log.i(TAG, "Get sample rate = " + sampleRate);
                textView.setText("sample rate = " + rate);
                break;
            }
        }
    }

    public void play(View view){
        Button playBtn = (Button) findViewById(R.id.playBtn);
        if(isPlaying){
            record.stop();
            track.pause();
            isPlaying = false;
            playBtn.setText("Play");
        } else {
            record.startRecording();
            track.play();
            isPlaying = true;
            playBtn.setText("Pause");
        }
    }

    class AudioThread extends Thread {
        private short[] lin = new short[1024];
        private int num = 0;
        private Activity activity = null;
        private boolean running = true;

        AudioThread(Activity activity) {
            am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            am.setMode(AudioManager.MODE_IN_COMMUNICATION);
            record.startRecording();
            track.play();
        }

        public void run() {
            while (running) {
                num = record.read(lin, 0, 1024);
                Log.i(TAG, "Mic read " + num + " short byte");
                track.write(lin, 0, num);
            }
        }

        public void stopRunning() {
            running = false;
        }
    }
}
