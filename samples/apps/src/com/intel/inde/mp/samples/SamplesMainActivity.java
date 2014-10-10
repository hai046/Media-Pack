//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SamplesMainActivity extends ExpandableListActivity {
    private ExpandableSamplesListAdapter samplesListAdapter;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main);

        samplesListAdapter = new ExpandableSamplesListAdapter(this);

        setListAdapter(samplesListAdapter);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int group, int child, long id) {
        ExpandableSamplesListAdapter.SampleItem sample = samplesListAdapter.getChild(group, child);

        Intent intent = null;

        try {
            intent = new Intent(SamplesMainActivity.this, Class.forName(sample.className));
        } catch (ClassNotFoundException e) {
            showToast("Something went wrong...");
        }

        startActivity(intent);

        return super.onChildClick(parent, view, group, child, id);
    }

    private void showToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }
}
