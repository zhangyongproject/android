/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blue.uyou.gamelauncher.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.blue.uyou.gamelauncher.Launcher;
import com.blue.uyou.gamelauncher.R;
import com.blue.uyou.gamelauncher.utils.AppUtils;
import com.blue.uyou.gamelauncher.utils.ScalingUtilities;
import com.blue.uyou.gamelauncher.utils.ScalingUtilities.ScalingLogic;

public class PagedViewIcon extends TextView implements OnClickListener {
	private static final String TAG = "PagedViewIcon";
	private Bitmap mIcon;
	private ApplicationInfo appInfo;
	private Paint paint;
	private Rect rect;
	private int appWidth;
	private int appHeight;
	// private int marginLeft;
	// private int marginTop;
	private String text;

	public PagedViewIcon(Context context) {
		super(context);
		init();
	}

	public PagedViewIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ApplicationInfo getAppInfo() {
		return this.appInfo;
	}

	private void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		int size = 38;
		if (Launcher.isHD()) {
			size = Math.round(size / 1.5f);
		}
		paint.setTextSize(size);
		setBackground(getResources().getDrawable(R.drawable.selector_item));
		rect = new Rect();
		setFocusable(true);
		setClickable(true);
		setOnClickListener(this);
		appWidth = getResources().getDimensionPixelOffset(
				R.dimen.app_icon_width);
		appHeight = getResources().getDimensionPixelOffset(
				R.dimen.app_icon_height);
		setTextSize(size);
		setSingleLine();
		setEllipsize(TruncateAt.END);
		setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

		// marginLeft = getResources().getDimensionPixelOffset(
		// R.dimen.app_icon_margin_left);
		// marginTop = getResources().getDimensionPixelOffset(
		// R.dimen.app_icon_margin_top);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 绘制Icon
		if (mIcon != null) {
			float x = getWidth() / 2 - mIcon.getWidth() / 2;
			float y = getHeight() / 2 - mIcon.getHeight() / 2;
			canvas.drawBitmap(mIcon, getScrollX() + x, getScaleY() + y, null);
		}
		if (!TextUtils.isEmpty(text)) {
			paint.getTextBounds(text, 0, text.length(), rect);
			paint.setColor(Color.WHITE);
			paint.setTextAlign(Align.LEFT);
			int paddingleft = 0;
			float x = (getWidth() / 2 - paddingleft)
					- (rect.width() / 2 - paddingleft);
			if (x < 0) {
				x = paddingleft;
			}
			int marginTop = 10;
			if (Launcher.isHD()) {
				marginTop = Math.round(marginTop / 1.5f);
			}
			float y = getHeight() - marginTop;
			canvas.drawText(text, getScrollX() + x, getScrollY() + y, paint);
		}
		super.onDraw(canvas);
	}

	public void fastApplyFromAppInfo(ApplicationInfo info) {
		if (info == null) {
			appInfo = null;
			setVisibility(View.INVISIBLE);
			return;
		}
		if (getVisibility() != View.VISIBLE) {
			setVisibility(View.VISIBLE);
		}
		if (info.iconBitmap.getWidth() != appWidth
				|| info.iconBitmap.getHeight() != appHeight) {
			mIcon = ScalingUtilities.createScaledBitmap(info.iconBitmap,
					appWidth, appHeight, ScalingLogic.FIT, false);
		} else {
			mIcon = info.iconBitmap;
		}
		appInfo = info;
		// this.text = String.valueOf(info.title);
		setText(info.title);
	}

	@Override
	public void onClick(View v) {
		if (appInfo == null) {
			return;
		}
		AppUtils.startActivity(getContext(), appInfo.intent);
	}
}
