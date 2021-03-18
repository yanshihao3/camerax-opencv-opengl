package com.zq.opencv.opengl.filter;

import android.content.Context;

import com.zq.opencv.R;

/**
 * 屏幕过滤器
 */
public class ScreenFilter extends AbstractFilter {

    public ScreenFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.base_frag);
    }

}
