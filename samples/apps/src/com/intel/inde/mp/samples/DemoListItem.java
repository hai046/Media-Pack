//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

public class DemoListItem {
    private String mTitle;
    private String mClassName;

    public DemoListItem(String title, String className) {
        mTitle = title;
        mClassName = className;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getClassName() {
        return mClassName;
    }
}
