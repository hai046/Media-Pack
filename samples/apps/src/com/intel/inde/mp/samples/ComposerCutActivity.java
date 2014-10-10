//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.intel.inde.mp.Uri;
import com.intel.inde.mp.samples.controls.TimelineItem;

public class ComposerCutActivity extends ActivityWithTimeline implements View.OnClickListener {
    TimelineItem mItem;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.composer_cut_activity);

        init();
    }

    private void init() {
        mItem = (TimelineItem) findViewById(R.id.timelineItem);
        mItem.setEventsListener(this);
        mItem.enableSegmentPicker(true);

        ((Button) findViewById(R.id.action)).setOnClickListener(this);
    }

    public void action() {
        Uri uri = mItem.getUri();

        if (uri == null) {
            showToast("Please select a valid video file first");

            return;
        }

        mItem.stopVideoView();

        int segmentFrom = mItem.getSegmentFrom();
        int segmentTo = mItem.getSegmentTo();

        Intent intent = new Intent();
        intent.setClass(this, ComposerCutCoreActivity.class);

        Bundle b = new Bundle();
        b.putString("srcMediaName1", mItem.getMediaFileName());
        intent.putExtras(b);
        b.putString("dstMediaPath", mItem.genDstPath(mItem.getMediaFileName(), "segment"));
        intent.putExtras(b);
        b.putLong("segmentFrom", segmentFrom);
        intent.putExtras(b);
        b.putLong("segmentTo", segmentTo);
        intent.putExtras(b);
        b.putString("srcUri1", uri.getString());
        intent.putExtras(b);

        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.action: {
                action();
            }
            break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mItem != null) {
            mItem.updateView();
        }
    }

}
