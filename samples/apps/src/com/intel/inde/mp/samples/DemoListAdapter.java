//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DemoListAdapter extends ArrayAdapter<DemoListItem> {

    private List<DemoListItem> mList;
    private Activity mContext;

    public DemoListAdapter(Activity context, List<DemoListItem> list) {
        super(context, R.layout.sample_list_item, list);

        mContext = context;
        mList = list;
    }

    static class ViewHolder {
        protected TextView mTitle;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        DemoListItem item = mList.get(position);

        View view = null;

        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();

            view = inflater.inflate(R.layout.sample_list_item, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.mTitle = (TextView) view.findViewById(R.id.itemTitle);

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.mTitle.setText(item.getTitle());

        return view;
    }

    public void updateDisplay() {
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
