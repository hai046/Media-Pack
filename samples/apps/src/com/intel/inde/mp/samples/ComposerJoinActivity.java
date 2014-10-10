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
import com.intel.inde.mp.Uri;
import com.intel.inde.mp.samples.controls.TimelineItem;

public class ComposerJoinActivity extends ActivityWithTimeline implements View.OnClickListener {
    TimelineItem item1 = null;
    TimelineItem item2 = null;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.composer_join_activity);

        init();
    }

    private void init() {
        item1 = (TimelineItem) findViewById(R.id.timelineItem1);
        item1.setEventsListener(this);
        item1.enableSegmentPicker(false);

        item2 = (TimelineItem) findViewById(R.id.timelineItem2);
        item2.setEventsListener(this);
        item2.enableSegmentPicker(false);

        findViewById(R.id.action).setOnClickListener(this);
    }

    public void action() {
        Uri uri1 = item1.getUri();
        Uri uri2 = item2.getUri();

        if (uri1 == null || uri2 == null) {
            showToast("Please select valid video files first");

            return;
        }

        item1.stopVideoView();
        item2.stopVideoView();

        Intent intent = new Intent();
        intent.setClass(this, ComposerJoinCoreActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("srcMediaName1", item1.getMediaFileName());
        intent.putExtras(bundle);
        bundle.putString("srcMediaName2", item2.getMediaFileName());
        intent.putExtras(bundle);
        bundle.putString("dstMediaPath", item1.genDstPath(item1.getMediaFileName(), "joined"));
        intent.putExtras(bundle);
        bundle.putString("srcUri1", uri1.getString());
        intent.putExtras(bundle);
        bundle.putString("srcUri2", uri2.getString());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (item1 != null) {
            item1.updateView();
        }

        if (item2 != null) {
            item2.updateView();
        }
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
}
