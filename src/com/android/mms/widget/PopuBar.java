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

package com.android.mms.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Context;
import android.content.res.Resources;
import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
@SuppressLint("NewApi")
public class PopuBar extends DialogFragment implements OnClickListener {

	private static float mScale = 0; // Used for supporting different screen
										// densities
	private static int mCustomAppIconSize = 32;
	private static int mDialogWidth = 50;
	private static int mDialogHeight = 180;
	private static int DIALOG_TOP_MARGIN = 8;
	private boolean mIsDialog = false;
	private boolean mIsPaused = true;
	private boolean mDismissOnResume = false;
	private int mX = -1;
	private int mY = -1;
	private int mMinTop; // Dialog cannot be above this location
	private int selectViewHeight;
	private Activity mActivity;
	private Context mContext;
	// Style of view
	public static final int FULL_WINDOW_STYLE = 0;
	public static final int DIALOG_WINDOW_STYLE = 1;
	private int mWindowStyle = DIALOG_WINDOW_STYLE;
	private Uri mUri;
	protected static final String BUNDLE_KEY_IS_DIALOG = "key_fragment_is_dialog";
	protected static final String BUNDLE_KEY_WINDOW_STYLE = "key_window_style";
	protected static final String BUNDLE_KEY_DELETE_DIALOG_VISIBLE = "key_delete_dialog_visible";
	protected static final String BUNDLE_KEY_EVENT_ID = "key_event_id";
	private long mEventId;
	private View mView;
	private Button moreBtn;
	private Button moreBtn2;//added by wangyouyou
	private Button copyBtn;
	private final int BUTTON_MORE = 0;
	private final int BUTTON_COPY = 1;
	private final int BUTTON_MORE_2 = 3;//added by wangyouyou
	
	@SuppressLint("NewApi")
	public PopuBar(Context context, Uri uri, boolean isDialog, int windowStyle) {
		Log.v("sjb", "EventInfoFragment....");
		Resources r = context.getResources();
		if (mScale == 0) {
			mScale = context.getResources().getDisplayMetrics().density;
			if (mScale != 1) {
				mCustomAppIconSize *= mScale;
				if (isDialog) {
					DIALOG_TOP_MARGIN *= mScale;
				}
			}
		}
		if (isDialog) {
			setDialogSize(r);
		}
		mIsDialog = isDialog;

		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		mUri = uri;

		mWindowStyle = windowStyle;
	}

	public PopuBar() {
		// TODO Auto-generated constructor stub
		super();
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v("sjb", "onActivityCreated....");

		if (savedInstanceState != null) {
			mIsDialog = savedInstanceState.getBoolean(BUNDLE_KEY_IS_DIALOG,
					false);
			mWindowStyle = savedInstanceState.getInt(BUNDLE_KEY_WINDOW_STYLE,
					DIALOG_WINDOW_STYLE);
		}

		if (mIsDialog) {
			applyDialogParams();
		}


	}

	@SuppressLint("NewApi")
	private void applyDialogParams() {
		Dialog dialog = getDialog();
		dialog.setCanceledOnTouchOutside(true);

		Window window = dialog.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		WindowManager.LayoutParams a = window.getAttributes();
		a.dimAmount = 0f;

		a.width = mDialogWidth;
		a.height = mDialogHeight;

		// On tablets , do smart positioning of dialog
		// On phones , use the whole screen

		if (mX != -1 || mY != -1) {
			a.x = mX - mDialogWidth / 2;
			a.y = mY - mDialogHeight*3/2 ;
			if (a.y < mMinTop) {
				a.y = mY+selectViewHeight;
				//moreBtn.setBackground(getResources().getDrawable(R.drawable.more_bg_2)); //del by wangyouyou
				//begin: added by wangyouyou
				moreBtn.setVisibility(View.GONE);
				moreBtn2.setVisibility(View.VISIBLE);
				//end: added by wangyouyou
			}
			a.gravity = Gravity.CENTER | Gravity.TOP;
		}
		window.setAttributes(a);
	}

	public void setDialogParams(int x, int y, int minTop,int height) {
		Log.v("sjb", "setDialogParams");
		mX = x;
		mY = y;
		mMinTop = minTop;
		selectViewHeight =height;
	}

	@SuppressLint("NewApi")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.v("sjb", "onAttach....");
		mActivity = activity;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v("sjb", "onCreateView....");
		if (savedInstanceState != null) {
			mIsDialog = savedInstanceState.getBoolean(BUNDLE_KEY_IS_DIALOG,
					false);
			mWindowStyle = savedInstanceState.getInt(BUNDLE_KEY_WINDOW_STYLE,
					DIALOG_WINDOW_STYLE);

		}

		if (mWindowStyle == DIALOG_WINDOW_STYLE) {
			mView = inflater.inflate(R.layout.popu_dialog_layout, container,
					false);
		} else {
			// mView = inflater.inflate(R.layout.event_info, container, false);
		}
		moreBtn = (Button) mView.findViewById(R.id.more);
		moreBtn.setTag(BUTTON_MORE);
		moreBtn.setOnClickListener(this);
		copyBtn = (Button) mView.findViewById(R.id.copy);
		copyBtn.setTag(BUTTON_COPY);
		copyBtn.setOnClickListener(this);
		//begin: added by wangyouyou
		moreBtn2 = (Button) mView.findViewById(R.id.more_2);
		moreBtn2.setTag(BUTTON_MORE_2);
		moreBtn2.setOnClickListener(this);
		//end: added by wangyouyou
		return mView;
	}

	@SuppressLint("NewApi")
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(BUNDLE_KEY_EVENT_ID, mEventId);

		outState.putBoolean(BUNDLE_KEY_IS_DIALOG, mIsDialog);
		outState.putInt(BUNDLE_KEY_WINDOW_STYLE, mWindowStyle);

	}

	@SuppressLint("NewApi")
	@Override
	public void onDestroyView() {

		super.onDestroyView();
	}

	@SuppressLint("NewApi")
	@Override
	public void onDestroy() {
              ((ComposeMessageActivity)mActivity).cancelSelected();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		mIsPaused = true;

		super.onPause();
		// Remove event deletion alert box since it is being rebuild in the
		// OnResume
		// This is done to get the same behavior on OnResume since the
		// AlertDialog is gone on
		// rotation but not if you press the HOME key

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mIsDialog) {
			setDialogSize(getActivity().getResources());
			applyDialogParams();
		}
		mIsPaused = false;

	}

	@Override
	public void onClick(View view) {
		Log.v("sjb", "onClick......");
		int tag = (Integer) view.getTag();
		switch (tag) {
		case BUTTON_COPY:
			Log.v("sjb", "onClick......1");
			((ComposeMessageActivity)mActivity).copyTextFromSelectedItem();
			dismiss();
			break;
		case BUTTON_MORE:
			Log.v("sjb", "onClick......2");
			((ComposeMessageActivity)mActivity).editMessageForOnClick();
			dismiss();
			break;
		//begin: added by wangyouyou
		case BUTTON_MORE_2:
			Log.v("sjb", "onClick......2");
			((ComposeMessageActivity)mActivity).editMessageForOnClick();
			dismiss();
			break;
		//end: added by wangyouyou
		default:
			break;
		}

	}

	private void setDialogSize(Resources r) {
		mDialogWidth = (int) r.getDimension(R.dimen.event_info_dialog_width);
		mDialogHeight = (int) r.getDimension(R.dimen.event_info_dialog_height);
	}

}
