//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples.controls;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.PopupWindow;
import com.intel.inde.mp.samples.R;

public class Popup extends PopupWindow implements OnTouchListener, PopupWindow.OnDismissListener {
    protected Context context;
    protected View contentView;

    public Popup(Context context) {
        super(context);

        this.context = context;

        init();
    }

    public void show(View anchor, boolean center) {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int contentWidth = contentView.getMeasuredWidth();
        int contentHeight = contentView.getMeasuredHeight();

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int xPos = 0;
        int yPos = 0;

        boolean onTop = true;
        boolean onLeft = true;

        int gravity = Gravity.NO_GRAVITY;

        if (center) {
            xPos = 0;
            yPos = 0;

            gravity = Gravity.CENTER;
        } else {
            int[] location = new int[2];

            anchor.getLocationOnScreen(location);

            Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

            if ((anchorRect.left + contentWidth) > screenWidth) {
                if (anchorRect.right - contentWidth < 0) {
                    xPos = screenWidth - contentWidth - 4;
                } else {
                    xPos = anchorRect.right - contentWidth;
                }

                onLeft = false;
            } else {
                xPos = anchorRect.left;
                onLeft = true;
            }

            if (anchorRect.top < screenHeight / 2) {
                yPos = anchorRect.bottom + 4;
                onTop = true;
            } else {
                yPos = anchorRect.top - contentHeight - 4;
                onTop = false;
            }

            if (onTop) {
                if (onLeft) {
                    setAnimationStyle(R.style.Popups_Down_Left);
                } else {
                    setAnimationStyle(R.style.Popups_Down_Right);
                }
            } else {
                if (onLeft) {
                    setAnimationStyle(R.style.Popups_Up_Left);
                } else {
                    setAnimationStyle(R.style.Popups_Up_Right);
                }
            }
        }

        setOnDismissListener(this);

        onShow();

        showAtLocation(anchor, gravity, xPos, yPos);
    }

    public void hide() {
        dismiss();
    }

    private void init() {
        setTouchInterceptor(this);

        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);

        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_popup));
    }

    public void setContentView(View view) {
        super.setContentView(view);

        contentView = view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            dismiss();

            return true;
        }

        return false;
    }

    public void onShow() {
    }

    @Override
    public void onDismiss() {

    }
}
