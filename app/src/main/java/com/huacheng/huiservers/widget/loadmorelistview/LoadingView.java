package com.huacheng.huiservers.widget.loadmorelistview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.huacheng.huiservers.R;


public class LoadingView extends LinearLayout {

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_refresh_footer, this);
    }


}
