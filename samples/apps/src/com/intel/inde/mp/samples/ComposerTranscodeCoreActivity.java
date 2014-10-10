//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.intel.inde.mp.*;
import com.intel.inde.mp.android.AndroidMediaObjectFactory;
import com.intel.inde.mp.android.AudioFormatAndroid;
import com.intel.inde.mp.android.VideoFormatAndroid;
import com.intel.inde.mp.domain.ISurfaceWrapper;
import com.intel.inde.mp.samples.controls.TranscodeSurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class ComposerTranscodeCoreActivity extends Activity implements SurfaceHolder.Callback {

    protected String srcMediaName1 = null;
    protected String srcMediaName2 = null;
    protected String dstMediaPath = null;
    protected com.intel.inde.mp.Uri mediaUri1;
    protected com.intel.inde.mp.Uri mediaUri2;

    protected MediaFileInfo mediaFileInfo = null;
    
    protected long duration = 0;

    protected ProgressBar progressBar;

    protected TextView pathInfo;
    protected TextView durationInfo;
    protected TextView effectDetails;

    protected TextView transcodeInfoView;

    ///////////////////////////////////////////////////////////////////////////

    protected AudioFormat audioFormat;
    protected VideoFormat videoFormat;

    // Transcode parameters

    // Video
    protected  int videoWidthOut;
    protected  int videoHeightOut;

    protected int videoWidthIn = 640;
    protected int videoHeightIn = 480;

    protected Spinner frameSizeSpinner;
    protected Spinner videoBitRateSpinner;

    protected final String videoMimeType = "video/avc";
    protected int videoBitRateInKBytes = 5000;
    protected final int videoFrameRate = 30;

    protected final int videoIFrameInterval = 1;
    // Audio
    protected final String audioMimeType = "audio/mp4a-latm";
    protected final int audioSampleRate = 48000;
    protected final int audioChannelCount = 2;

    protected final int audioBitRate = 96 * 1024;
    protected Button buttonStop;

    protected Button buttonStart;

    ///////////////////////////////////////////////////////////////////////////

    // Media Composer parameters and logic

    protected MediaComposer mediaComposer;

    private boolean isStopped = false;

    public IProgressListener progressListener = new IProgressListener() {
        @Override
        public void onMediaStart() {

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(0);
                        updateUI(true);
                    }
                });
            } catch (Exception ex) {
            }
        }

        @Override
        public void onMediaProgress(float progress) {

            final float mediaProgress = progress;

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) (progressBar.getMax() * mediaProgress));
                    }
                });
            } catch (Exception ex) {
            }
        }

        @Override
        public void onMediaDone() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isStopped) {
                            return;
                        }
                        updateUI(false);
                        reportTranscodeDone();
                    }
                });
            } catch (Exception ex) {
            }
        }

        @Override
        public void onMediaPause() {

        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    updateUI(false);
                    showMessageBox("Transcoding failed.", null);
                    }
                });
            } catch (Exception ex) {
            }
        }
    };
    protected AndroidMediaObjectFactory factory;

    /////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.composer_transcode_core_activity);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);

        pathInfo =  (TextView) findViewById(R.id.pathInfo);
        durationInfo = (TextView) findViewById(R.id.durationInfo);
        effectDetails = (TextView) findViewById(R.id.effectDetails);

        initVideoSpinners();

        transcodeInfoView = (TextView) findViewById(R.id.transcodeInfo);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        ////
        TranscodeSurfaceView transcodeSurfaceView = (TranscodeSurfaceView) findViewById(R.id.transcodeSurfaceView);
        transcodeSurfaceView.getHolder().addCallback(this);
        ////

        setupUI();

        getActivityInputs();

        getFileInfo();
        printFileInfo();

        transcodeSurfaceView.setImageSize(videoWidthIn, videoHeightIn);

        updateUI(false);
    }

    @Override
    protected void onDestroy() {
        if (mediaComposer != null) {
            mediaComposer.stop();
            isStopped = true;
        }
        super.onDestroy();
    }

    protected void setupUI()
    {
        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startTranscode();
            }
        });

        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTranscode();
            }
        });
    }

    protected  void initVideoSpinners()
    {
        ArrayAdapter<CharSequence> adapter;
        // Video parameters spinners

        frameSizeSpinner = (Spinner) findViewById(R.id.frameSize_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                                                  R.array.frame_size_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frameSizeSpinner.setAdapter(adapter);
        frameSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] frameSizeContainer = frameSizeSpinner.getSelectedItem().toString().split("x", 2);
                videoWidthOut = Integer.valueOf(frameSizeContainer[0].trim());
                videoHeightOut = Integer.valueOf(frameSizeContainer[1].trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        videoBitRateSpinner = (Spinner) findViewById(R.id.videoBitRate_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                                                  R.array.video_bit_rate_values, android.R.layout.simple_spinner_item);
        videoBitRateSpinner.setAdapter(adapter);
        videoBitRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                videoBitRateInKBytes = Integer.valueOf(videoBitRateSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    ///////////////////////////////////////////////////////////////////////////

    protected void getActivityInputs() {

        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new com.intel.inde.mp.Uri(b.getString("srcUri1"));
    }

    /////////////////////////////////////////////////////////////////////////

    protected void getFileInfo() {

        //Log.d(TAG, "getFileInfo");
        try {
        	mediaFileInfo = new MediaFileInfo(new AndroidMediaObjectFactory(getApplicationContext()));
            mediaFileInfo.setUri(mediaUri1);

            duration = mediaFileInfo.getDurationInMicroSec();

            audioFormat = (AudioFormat) mediaFileInfo.getAudioFormat();
            videoFormat = (VideoFormat) mediaFileInfo.getVideoFormat();

            videoWidthIn = videoFormat.getVideoFrameSize().width();
            videoHeightIn = videoFormat.getVideoFrameSize().height();           

        } catch (Exception e) {
            showMessageBox(e.getMessage(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }
    }    
    
    protected void displayVideoFrame(SurfaceHolder holder) {
        try {
            ISurfaceWrapper surface = AndroidMediaObjectFactory.Converter.convert(holder.getSurface());
            mediaFileInfo.setOutputSurface(surface);
            
            ByteBuffer buffer = ByteBuffer.allocate(1);
            mediaFileInfo.getFrameAtPosition(100, buffer);

        } catch (Exception e) {
            showMessageBox(e.getMessage(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        displayVideoFrame(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    ///////////////////////////////////////////////////////////////////////////

    protected void configureVideoEncoder(MediaComposer mediaComposer, int width, int height) {

        VideoFormatAndroid videoFormat = new VideoFormatAndroid(videoMimeType, width, height);

        videoFormat.setVideoBitRateInKBytes(videoBitRateInKBytes);
        videoFormat.setVideoFrameRate(videoFrameRate);
        videoFormat.setVideoIFrameInterval(videoIFrameInterval);

        mediaComposer.setTargetVideoFormat(videoFormat);
    }

    protected void configureAudioEncoder(MediaComposer mediaComposer) {

        AudioFormatAndroid audioFormat = new AudioFormatAndroid(audioMimeType, audioSampleRate, audioChannelCount);

        audioFormat.setAudioBitrateInBytes(audioBitRate);
        audioFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        mediaComposer.setTargetAudioFormat(audioFormat);
    }

    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {

        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);
    }

    protected void transcode() throws Exception {

        factory = new AndroidMediaObjectFactory(getApplicationContext());
        mediaComposer = new MediaComposer(factory, progressListener);
        setTranscodeParameters(mediaComposer);
        mediaComposer.start();
    }

    public void startTranscode() {

        try {
            transcode();       
            
        } catch (Exception e) {

            buttonStart.setEnabled(false);

            showMessageBox(e.getMessage(), new DialogInterface.OnClickListener() {
                @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
        }        
    }

    public void stopTranscode() {
    
        mediaComposer.stop();
    }

    private void reportTranscodeDone() {

        String message = "Transcoding finished.";

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            progressBar.setVisibility(View.INVISIBLE);
            findViewById(R.id.buttonStart).setVisibility(View.GONE);
            findViewById(R.id.buttonStop).setVisibility(View.GONE);

            OnClickListener l = new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        playResult();
                    }
                };

            ImageButton ib = (ImageButton) findViewById(R.id.imageButtonPlay);
            ib.setVisibility(View.VISIBLE);
            ib.setOnClickListener(l);
            }
        };

        showMessageBox(message, listener);
    }

    protected void showMessageBox(String message, DialogInterface.OnClickListener listener) {

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(message);
        b.setPositiveButton("OK", listener);
        AlertDialog d = b.show();
        ((TextView)d.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
    }

    private void playResult() {

        String videoUrl = "file:///" + dstMediaPath;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.parse(videoUrl);
        intent.setDataAndType(data, "video/mp4");
        startActivity(intent);
    }

    private void updateUI(boolean inProgress) {
        buttonStart.setEnabled(!inProgress);
        buttonStop.setEnabled(inProgress);

        if(inProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    ////////////////////////////////////////////////////////////////////////////    

    protected void printPaths() {

        pathInfo.setText(String.format("srcMediaFileName = %s\ndstMediaPath = %s\n", srcMediaName1, dstMediaPath));
    }

    protected void getDstDuration() {
    	
    }    
    
    protected void printDuration() {

        getDstDuration();
    	
        durationInfo.setText(String.format("Duration = %d sec", TimeUnit.MICROSECONDS.toSeconds(duration)));
    }   

    protected void printEffectDetails() {

    }
    
    protected void printFileInfo() {
        printPaths();
        printDuration();
        printEffectDetails();
    }      
}


