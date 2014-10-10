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
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.intel.inde.mp.samples.controls.TimelineItem;

public class ActivityWithTimeline extends Activity implements TimelineItem.TimelineItemEvents {
    protected static final int IMPORT_FROM_GALLERY_REQUEST = 1;

    TimelineItem mItemToPick;

    @Override
    public void onOpen(TimelineItem item) {
        mItemToPick = item;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        startActivityForResult(intent, IMPORT_FROM_GALLERY_REQUEST);
    }

    @Override
    public void onDelete(TimelineItem item) {
        item.setMediaUri(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {

            case IMPORT_FROM_GALLERY_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Uri selectedVideo = intent.getData();

                    if ( selectedVideo == null) {
                        showToast("Invalid URI");
                        return;
                    }

                    Cursor cursor = getContentResolver().query(selectedVideo, null, null, null, null);

                    if (cursor != null) {
                        cursor.moveToFirst();

                        int idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME);

                        if (idx != -1) {
                            String displayName = cursor.getString(idx);

                            mItemToPick.setMediaFileName(displayName);

                            com.intel.inde.mp.Uri uri = new com.intel.inde.mp.Uri(selectedVideo.toString());

                            try {
                                mItemToPick.setMediaUri(uri);
                            } catch (IllegalArgumentException ex) {
                                showToast(ex.getMessage());
                            }
                        } else {
                            showToast("Error while importing video from gallery");
                        }

                        cursor.close();
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void showToast(String title)
    {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }

    public void showMessageBox(String message, DialogInterface.OnClickListener listener) {

        if (message == null) { message = ""; }

        if (listener == null) {
            listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            };
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(message);
        b.setPositiveButton("OK", listener);
        AlertDialog d = b.show();

        ((TextView)d.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
    }
}
