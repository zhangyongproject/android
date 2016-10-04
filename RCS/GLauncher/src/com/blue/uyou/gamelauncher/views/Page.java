package com.blue.uyou.gamelauncher.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;
import android.widget.LinearLayout;

public class Page extends LinearLayout {
    private GridLayout gridLayout;

    public Page(Context context) {
        super(context);
    }
    public Page(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridLayout getGridLayout() {
        return gridLayout;
    }

    public void setGridLayout(GridLayout gridLayout) {
        this.gridLayout = gridLayout;
    }
}
