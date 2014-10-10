//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import com.intel.inde.mp.Uri;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioReader {
    private static final int TIMEOUT_USEC = 10000;

    private MediaExtractor audioExtractor;
    private MediaCodec audioDecoder;
    private boolean noEOS = true;

    private ByteBuffer[] audioDecoderInputBuffers = null;
    private ByteBuffer[] audioDecoderOutputBuffers = null;
    private ByteBuffer[] audioEncoderInputBuffers = null;
    private ByteBuffer[] audioEncoderOutputBuffers = null;
    private MediaCodec.BufferInfo audioDecoderOutputBufferInfo = null;
    private MediaFormat decoderOutputAudioFormat = null;
    private Uri uri;

    public void setFileUri(Uri uri) {
        this.uri = uri;
    }

    public void start(Context context) {
        audioExtractor = createExtractor(context);

        int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
        MediaFormat inputFormat = audioExtractor.getTrackFormat(audioInputTrack);

        // Create a MediaCodec for the decoder, based on the extractor's format.
        // Create a MediaCodec for the decoder, based on the extractor's format.
        audioDecoder = createAudioDecoder(inputFormat);

        audioDecoderInputBuffers = audioDecoder.getInputBuffers();
        audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
        audioDecoderOutputBufferInfo = new MediaCodec.BufferInfo();
    }

    public boolean read(ByteBuffer byteBuffer) {
        boolean noData = true;
        while (noData && noEOS) {
            int decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(TIMEOUT_USEC);

            if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            }

            if (decoderInputBufferIndex >= 0) {
                ByteBuffer decoderInputBuffer = audioDecoderInputBuffers[decoderInputBufferIndex];
                int size = audioExtractor.readSampleData(decoderInputBuffer, 0);

                long presentationTime = audioExtractor.getSampleTime();

                if (size >= 0) {
                    audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, audioExtractor.getSampleFlags());
                }

                noEOS = audioExtractor.advance();
            }

            int decoderOutputBufferIndex = audioDecoder.dequeueOutputBuffer(audioDecoderOutputBufferInfo, TIMEOUT_USEC);
            if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            }
            if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();

            }
            if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                decoderOutputAudioFormat = audioDecoder.getOutputFormat();
            }

            if (decoderOutputBufferIndex >= 0) {
                ByteBuffer decoderOutputBuffer = audioDecoder.getOutputBuffers()[decoderOutputBufferIndex];

                byteBuffer.limit(audioDecoderOutputBufferInfo.size);
                byteBuffer.position(0);
                decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.size);
                decoderOutputBuffer.position(0);
                byteBuffer.put(decoderOutputBuffer);
                audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);

                noData = false;
            }
        }

        return !noData;
    }

    ;

    private MediaExtractor createExtractor(Context context) {
        MediaExtractor extractor;
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, android.net.Uri.parse(uri.getString()), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractor;
    }

    private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    private static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }

    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }

    private MediaCodec createAudioDecoder(MediaFormat inputFormat) {
        MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        decoder.configure(inputFormat, null, null, 0);
        decoder.start();
        return decoder;
    }
}
