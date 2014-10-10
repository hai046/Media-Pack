//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

public class FPSCounter
{
	private long mTimeNow;
	private long mTimePrev;
	private long mTimeElapsed;
	
	private int mFrames;
	
	private int mFPS;

    private int mEveryXFrames;
	
	public FPSCounter(int xFrames)
	{
		mFrames = 0;
        mEveryXFrames = xFrames;
	}
	
	public boolean update()
	{		
		mFrames = (mFrames + 1) % mEveryXFrames;
		
		if(mFrames == 0)
		{
			mTimeNow = System.currentTimeMillis();
			mTimeElapsed = (mTimeNow - mTimePrev) / mEveryXFrames;
			mTimePrev = mTimeNow;

			mFPS = (int)(1000.0f / (float)mTimeElapsed);

            return true;
		}
		
		return false;
	}

    public int fps()
    {
        return mFPS;
    }
}
