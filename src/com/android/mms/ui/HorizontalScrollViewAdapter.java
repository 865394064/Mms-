package com.android.mms.ui;
import java.util.List;  
import com.android.mms.R;
import android.content.Context;  
import android.graphics.Bitmap;
import android.graphics.Matrix;//added by xiashuaishuai on 20150131 for Bug[3294]
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.ImageView;
import android.widget.BaseAdapter; 
public class HorizontalScrollViewAdapter extends BaseAdapter {
    private Context mContext;  
    private LayoutInflater mInflater;   
    private List<Bitmap> imagebm;
    private List<Integer>  imageId;
    private boolean selectflag=false;
   public HorizontalScrollViewAdapter(Context context,List<Bitmap> imagebm,boolean flag,List<Integer> imageId)  
    {  
        this.mContext = context;  
        mInflater = LayoutInflater.from(context);  
        this.imagebm =imagebm;
        this.selectflag=flag;
        this.imageId=imageId;
    }  
  
    public int getCount()  
    {  
        return imagebm.size();  
    }  
  
    public Object getItem(int position)  
    {  
        return imagebm.get(position);  
    }  

	 public long getItemId(int position)  
    {  
        return position;  
    }  
	 
    public View getView(int position, View convertView, ViewGroup parent)  
    {  
        
            convertView = mInflater.inflate(  
                    R.layout.images_item, parent, false);  
           ImageView img = (ImageView) convertView  
                    .findViewById(R.id.imageshow_View);
	   ImageView  selected=(ImageView)convertView  
                    .findViewById(R.id.isselected);
	   if(selectflag&&mContext.getResources().getConfiguration().orientation==1){
        	    img.setImageBitmap(imagebm.get(position));
                    if(checkId(position)){
                          selected.setVisibility(View.VISIBLE);
                    }else{
                          selected.setVisibility(View.GONE);
                    }
           }else{
        	    Bitmap bm = small(imagebm.get(position));  
        	    img.setImageBitmap(bm);
        	   if(checkId(position)){
                          selected.setVisibility(View.VISIBLE);
	            }else{
	                  selected.setVisibility(View.GONE);
	           }
          }
          return convertView;  
    }  
    private boolean checkId(int position){
	   for(int i=0;i<imageId.size();i++){
		     if(imageId.get(i)==position) {
		    	 return true;
		     } 
	   }
	   return false;
    }
    private static Bitmap small(Bitmap bitmap) {
    	  Matrix matrix = new Matrix();
    	  matrix.postScale(0.5f,0.5f);
    	  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    	  return resizeBmp;
    }

}
