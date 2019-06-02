package com.android.mihailojoksimovic.audiorecognizer;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.mihailojoksimovic.audiorecognizer.libs.ApiClient;
import com.android.mihailojoksimovic.audiorecognizer.libs.FingerprintExtractor;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import org.json.*;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG                         = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION    = 200;

    private static final int RECORDER_SAMPLERATE                = 11025;
    private static final int RECORDER_CHANNELS                  = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING            = AudioFormat.ENCODING_PCM_16BIT;

    private static String mFileName                             = null;

    private boolean isRecording                                 = false;
    private int bufferSize                                      = 0;

    private short[] amplitudes                                  = null;

    private AudioRecord recorder;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Button button             = (Button) findViewById(R.id.listenButton);
        final Button stopButton         = (Button) findViewById(R.id.stopButton);
        final Button apiResendButton    = (Button) findViewById(R.id.apiResend);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showRecordingInProgress();

                button.setVisibility(View.INVISIBLE);

                stopButton.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            record();
                        } catch (IOException ex) {
                            Log.e(LOG_TAG, "Recording failed, shit!");
                        }
                    }
                }).start();
            }
        });



        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;

                recorder.stop();
                recorder.release();

                stopButton.setVisibility(View.INVISIBLE);
                button.setVisibility(View.VISIBLE);
            }
        });

        final Button playButton = (Button) findViewById(R.id.play);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, 2048, AudioTrack.MODE_STREAM);

                audioTrack.play();

                try {
                    FileInputStream fileInputStream = new FileInputStream(getFilePath());

                    byte[] buffer   = new byte[1024];

                    while (true) {
                        int n = fileInputStream.read(buffer);

                        if (n > 0) {
                            audioTrack.write(buffer, 0, n);
                        }

                        if (n==-1) {
                            break;
                        }
                    }


                } catch (FileNotFoundException ex) {
                    Log.e(LOG_TAG, "File is not found!!");
                } catch (IOException ex) {
                    Log.e(LOG_TAG, "Failed reading data from file!!");
                }

            }
        });

        apiResendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (amplitudes != null) {
                    ApiClient.matchAgainstSamples(amplitudes);
                }
            }
        });

    }

    private static File getFilePath() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, "pesma.pcm");

        return file;
    }

    private void showRecordingInProgress() {

    }

    public void record() throws IOException {

//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File file = getFilePath();

        OutputStream outputStream;

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException ex) {
            Log.e(LOG_TAG, "Unable to open file for recording!");

            return;
        }


//        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        bufferSize  = 2048;

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize
        );

        recorder.startRecording();

        isRecording = true;

        short[] buffer          = new short[bufferSize];

        ShortBuffer shortBuffer = ShortBuffer.allocate(RECORDER_SAMPLERATE * 10);

        while (isRecording) {
            int n = recorder.read(buffer, 0, buffer.length);

            if (n == -1) {
                break;
            }

            for (int i = 0; i < n; i++) {
                byte[] bytesToWrite = new byte[2];

                bytesToWrite[0] = (byte) (buffer[i] & 0xFF);
                bytesToWrite[1] = (byte) ((buffer[i] >> 8) & 0xFF);

                outputStream.write(bytesToWrite, 0, 2);

                shortBuffer.put(buffer[i]);
            }
        }

        amplitudes = shortBuffer.array();

        ApiClient.matchAgainstSamples(amplitudes);

        Log.d(LOG_TAG, "Finished writing to file!");

        outputStream.close();
    }


}
