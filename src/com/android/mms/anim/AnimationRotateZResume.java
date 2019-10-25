package com.android.mms.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class AnimationRotateZResume  extends Animation{
    private float mCenterX ;						//记录View的中间坐标  
    private float mCenterY ;
    private float mDistance;
    private Camera mCamera = new Camera();  
    public AnimationRotateZResume(float distance) {  
    	mDistance = distance;
    }  
    

    public void initialize(int width, int height, int parentWidth,  
           int parentHeight) {  
        super.initialize(width, height, parentWidth, parentHeight);  
        //初始化中间坐标值  
        mCenterX = width*0.5f;   
        mCenterY = height*0.5f;  
        //setDuration(160);  
        //setFillAfter(true);  
        setInterpolator(new AccelerateDecelerateInterpolator());  
        

    }  

      
    protected void applyTransformation(float interpolatedTime,  
           Transformation t) {   
        final Matrix matrix = t.getMatrix(); 
        mCamera.save();  
        mCamera.rotateZ(mDistance - mDistance*interpolatedTime);
        mCamera.getMatrix(matrix);  
        matrix.preTranslate(-mCenterX, -mCenterY);  
        matrix.postTranslate(mCenterX, mCenterY);  
        mCamera.restore();  
    }  
	
	
}
