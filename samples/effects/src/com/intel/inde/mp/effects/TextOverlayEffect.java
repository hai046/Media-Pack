//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import com.intel.inde.mp.domain.graphics.IEglUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextOverlayEffect extends OverlayEffect {

    private final float UPDATE_RATE = 2;
    private Paint paint;

    private int frameCount = 0;
    private float nextUpdate = 0.0f;
    private float fps = 0.0f;
    private float cpuUsage = 0.0f;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public TextOverlayEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);

        paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setAlpha(230);
        paint.setTextSize(40);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        update();
        canvas.drawText(String.format("FPS: %.0f", fps), 20, 60, paint);
        canvas.drawText(String.format("CPU: %.0f", cpuUsage * 100) + "%", 20, 110, paint);
    }

    private void update() {
        frameCount++;
        if (SystemClock.currentThreadTimeMillis() > nextUpdate * 1000) {
            nextUpdate += 1 / UPDATE_RATE;
            fps = frameCount * UPDATE_RATE;
            frameCount = 0;

            executor.execute(new Runnable() {
                public void run() {
                    cpuUsage = readCpuUsage();
                }
            });
        }
    }

    private float readCpuUsage() {
        RandomAccessFile reader = null;
        try {
            reader = new RandomAccessFile("/proc/stat", "r");
            String line = reader.readLine();
            line = line.replace("  ", " ");

            String[] split = line.split(" ");

            long idleTimeInitial = Long.parseLong(split[4]);
            long busyTimeInitial = Long.parseLong(split[1]) + Long.parseLong(split[2]) + Long.parseLong(split[3])
                    + Long.parseLong(split[5]) + Long.parseLong(split[6]) + Long.parseLong(split[7]);

            try {
                Thread.sleep(300);
            } catch (Exception e) {}

            reader.seek(0);
            line = reader.readLine();
            line = line.replace("  ", " ");
            reader.close();

            split = line.split(" ");

            long idleTimeFinal = Long.parseLong(split[4]);
            long busyTimeFinal = Long.parseLong(split[1]) + Long.parseLong(split[2]) + Long.parseLong(split[3])
                    + Long.parseLong(split[5]) + Long.parseLong(split[6]) + Long.parseLong(split[7]);

            return (float)(busyTimeFinal - busyTimeInitial) / ((busyTimeFinal + idleTimeFinal) - (busyTimeInitial + idleTimeInitial));

        } catch (IOException ex) {
            ex.printStackTrace();
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }
}
