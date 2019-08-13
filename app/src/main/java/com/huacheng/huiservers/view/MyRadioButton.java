package com.huacheng.huiservers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class MyRadioButton extends android.support.v7.widget.AppCompatRadioButton {

	public MyRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyRadioButton(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//获取设置的图片
		Drawable[] drawables = getCompoundDrawables();
		if (drawables != null) {
			//第一个是left
			Drawable drawableLeft = drawables[0];
			if (drawableLeft != null) {
//				//获取文字的宽度
//				float textWidth = getPaint().measureText(getText().toString());
//				int drawablePadding = getCompoundDrawablePadding();
//				int drawableWidth = 0;
//				drawableWidth = drawableLeft.getIntrinsicWidth();
//				float bodyWidth = textWidth + drawableWidth + drawablePadding;
//				int y = getWidth();
//				canvas.translate((getWidth() - bodyWidth) / 2, 0);
			}
		}
		super.onDraw(canvas);
	}
}

