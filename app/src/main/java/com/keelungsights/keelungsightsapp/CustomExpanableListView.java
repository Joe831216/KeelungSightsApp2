package com.keelungsights.keelungsightsapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * Created by Joe on 2017/11/14.
 * 解決ExpanableListView高度不正常問題
 */

public class CustomExpanableListView extends ExpandableListView {

    public CustomExpanableListView(Context context) {
        super(context);
    }

    public CustomExpanableListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CustomExpanableListView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
