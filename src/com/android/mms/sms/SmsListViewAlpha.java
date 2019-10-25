package com.android.mms.sms;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.util.Log;
/**
 * 短信查询时，浮在上面的那个阴影部分
 * @author tony   
 *
 */
public class SmsListViewAlpha extends View{
      private int top;
      private int alpha;
      private static final long ANIMATION_INTERVAL = 70;
	public SmsListViewAlpha(Context context) {
		this(context, null);
	}

	public SmsListViewAlpha(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public SmsListViewAlpha(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);       
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint mInnerPaint = new Paint();
		mInnerPaint.setAlpha(alpha);
		mInnerPaint.setAntiAlias(true);
    	RectF drawRect = new RectF();
    	int width =  ((ViewGroup)this.getParent()).getWidth();
    	
    	drawRect.set(0, top, width, getContext().getWallpaperDesiredMinimumHeight());
    	canvas.drawRect(drawRect, mInnerPaint);
	Log.v("sjb","top =========="+top+"  alpha ="+alpha);
        layout(0,0,width,getContext().getWallpaperDesiredMinimumHeight());
       if(alpha<=90){
           alpha+=10;
	    postInvalidateDelayed(ANIMATION_INTERVAL);
       }

	}
	public void showShadow(int top){
           this.top = top;
	    alpha = 0;
	    setVisibility(View.VISIBLE);
	}
	

}
