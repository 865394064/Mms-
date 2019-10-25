package com.android.mms;


import com.android.mms.ui.ConversationList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.Window;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.graphics.Color; 
import android.content.ComponentName;


public class ContactRelativedActivity extends Activity{
	private TextView name;
	private TextView mDetailDone;
	private ImageView call_people;
	private ImageView peopleInfoIB;
	private String mDetailString;
	private String mDetailSubString;
	private Button mPhotoLeftButton;
	private Button mAttachRightButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contact_relatived_layout);
		mDetailString = getIntent().getExtras().getString("detail_title");
		mDetailSubString = getIntent().getExtras().getString("detail_sub_title");
		
		name = (TextView) findViewById(R.id.people_number_or_name);
		mDetailDone = (TextView) findViewById(R.id.contact_detail_done);
		peopleInfoIB = (ImageButton)findViewById(R.id.people_info);
		call_people = (ImageButton)findViewById(R.id.call_people);
		mPhotoLeftButton = (Button)findViewById(R.id.photo_left_button);
		mAttachRightButton = (Button)findViewById(R.id.attach_right_button);
		name.setText(mDetailString);
		android.util.Log.d("wyy", "title = "+mDetailString+" , sub = "+mDetailSubString);
		mDetailDone.setOnClickListener(mListener);
		peopleInfoIB.setOnClickListener(mListener);
		call_people.setOnClickListener(mListener);	
		mPhotoLeftButton.setOnClickListener(mListener);	
		mAttachRightButton.setOnClickListener(mListener);	  
	}

	private OnClickListener mListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
			case R.id.contact_detail_done:
				//mDetailDone.setTextColor("ff007aff");
				
				finish();
				break;
			case R.id.people_info:
				finish();
				break;
			case R.id.call_people:
			android.provider.Settings.System.putInt(getContentResolver(),"call_by_system_telecom_from_mms",1);
				if(mDetailSubString != null) {//com.android.phone.InCallScreen.SHOW_ACTION
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+mDetailSubString));  
					/*intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ComponentName cn = new ComponentName("com.android.dialer", "com.android.incallui.InCallActivity");
					intent.setComponent(cn);*/
					    startActivity(intent);
	                		
				} else {
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+mDetailString));  
					/*intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					ComponentName cn = new ComponentName("com.android.dialer", "com.android.incallui.InCallActivity");
					intent.setComponent(cn);*/
					//intent.setAction("android.intent.action.CALL");
					//intetnt.setClass(this,CallActivity.class);
	                		//startActivity(intent);
					    startActivity(intent);
				}
				break;
			case R.id.photo_left_button:
				mPhotoLeftButton.setTextColor(Color.parseColor("#FFFFFF"));
				mAttachRightButton.setTextColor(Color.parseColor("#007aff"));
				mPhotoLeftButton.setBackgroundResource(R.drawable.img_selected);
				mAttachRightButton.setBackgroundResource(R.drawable.annex_unselect);
				break;
			case R.id.attach_right_button:
				mPhotoLeftButton.setBackgroundResource(R.drawable.img_unselect);
				mAttachRightButton.setBackgroundResource(R.drawable.annex_selected);
				mPhotoLeftButton.setTextColor(Color.parseColor("#007aff"));
				mAttachRightButton.setTextColor(Color.parseColor("#FFFFFF"));
				break;

			default:
				break;
			}
		}
	};
			
		
			
}
