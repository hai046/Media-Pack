//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.intel.inde.mp.samples.R;

public class PopupMessage extends LinearLayout
{
	private Context		context;
	private TextView	textView;

	public PopupMessage(Context context)
	{
		super(context);

		init(context, null, 0);
	}

	public PopupMessage(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		init(context, attrs, 0);
	}

	public PopupMessage(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle)
	{
        this.context = context;

		LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		inflater.inflate(R.layout.popup_message, this, true);

        textView = (TextView)findViewById(R.id.message);
	}
	
	public void show(String message)
	{
		textView.setText(message);
		
		setVisibility(View.VISIBLE);
	}
	
	public void hide()
	{
		setVisibility(View.INVISIBLE);    
	}
}
