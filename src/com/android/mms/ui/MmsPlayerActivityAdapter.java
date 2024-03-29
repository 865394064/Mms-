/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Browser;
import android.provider.ContactsContract;
import android.text.ClipboardManager;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.os.SystemProperties;

import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.data.Conversation;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MmsPlayerActivity.OnDataSetChangedListener;
import com.mediatek.encapsulation.com.mediatek.banyan.widget.EncapsulatedMTKImageView;
import com.mediatek.encapsulation.com.mediatek.common.featureoption.EncapsulatedFeatureOption;
import com.mediatek.encapsulation.MmsLog;
import com.android.mms.util.SmileyParser2;

import android.content.ActivityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;

/** M:
 * The back-end data adapter for MmsPlayerActivity.
 */
public class MmsPlayerActivityAdapter extends BaseAdapter {
    private static final String TAG = "Mms/MmsPlayerAdapter";
    private static final boolean LOCAL_LOGV = false;

    private final LayoutInflater mFactory;
    private ArrayList<MmsPlayerActivityItemData> mListItem;
    private int mAllCount;
    private Context mContext;
    private float textSize = 0;
    private DisplayMetrics mDisplayMetrics;
    private HashMap<Integer, View> mListItemViewCache = new HashMap<Integer, View>();
    private SmileyParser2 parser = SmileyParser2.getInstance();
    
    public void setTextSize(float size) {
        textSize = size;
    }

    public MmsPlayerActivityAdapter(Context context, ArrayList<MmsPlayerActivityItemData> listItem) {
        mFactory = LayoutInflater.from(context);
        mListItem = listItem;
        mAllCount = mListItem.size();
        mContext = context;
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    public void onMovedToScrapHeap(View view) {
    }

    public ArrayList<MmsPlayerActivityItemData> getListItem() {
        return mListItem;
    }
    
    @Override
    public int getCount() {
        return mAllCount;
    }

    @Override
    public MmsPlayerActivityItemData getItem(int arg0) {
        return mListItem.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        MmsLog.i(TAG, "getView, for position " + arg0);
        MmsPlayerActivityItem itemView = (MmsPlayerActivityItem) mListItemViewCache.get(arg0);
        if (itemView != null) {
            String text = mListItem.get(arg0).getText();
            if (text != null) {
                TextView mText = itemView.getCurrentTextView();
                MmsLog.i(TAG, "getView(): text view is null? " + (mText == null));
                if (mText != null && textSize != 0) {
                    MmsLog.i(TAG, "getView(): before set text size, textSize = " + mText.getTextSize());
                    mText.setTextSize(textSize);
                    MmsLog.i(TAG, "getView(): after set text size, textSize = " + mText.getTextSize());
                }
            }
            MmsLog.i(TAG, "getView(): from cache.");
            return itemView;
        }
        MmsLog.i(TAG, "getView(): create new one.");

        itemView = (MmsPlayerActivityItem) mFactory.inflate(R.layout.mms_player_activity_item, null);

        TextView mPageIndex = (TextView) itemView.findViewById(R.id.page_index);
        EncapsulatedMTKImageView mImage =(EncapsulatedMTKImageView)itemView.findViewById(R.id.image);
        //MTKImageView mImage =(MTKImageView)itemView.findViewById(R.id.image);
        ImageView mVideo = (ImageView) itemView.findViewById(R.id.video);
        ImageView mVideoPlayBtn = (ImageView) itemView.findViewById(R.id.video_play_overlay);
        View mAudio = (View) itemView.findViewById(R.id.audio);
        TextView mAudioName = (TextView) itemView.findViewById(R.id.audio_name);
        ImageView mAudioIcon = (ImageView) itemView.findViewById(R.id.audio_icon);
        TextView mText = (TextView) itemView.findViewById(R.id.top_text);

        final MmsPlayerActivityItemData item = mListItem.get(arg0);

        // show page index
        String index = mContext.getResources().getString(R.string.page, arg0 + 1);
        mPageIndex.setText(index);
        
        // show image
        final Uri imageUri = item.getImageUri();
        final String imageType = item.getImageType();
        if (imageUri != null) {
            MmsLog.i(TAG, "set image: "+ imageUri);
            mImage.setImageURI(imageUri);
            mImage.setVisibility(View.VISIBLE);
            mImage.setClickable(true);
            mImage.setOnClickListener(new mediaClick(imageUri, imageType, ""));
        } else {
            mImage.setVisibility(View.GONE);
        }
        
        // show video thumbnail
        Bitmap t = item.getVideoThumbnail();
        final Uri videoUri = item.getVideoUri();
        final String videoType = item.getVideoType();
        if (t != null) {
            MmsLog.i(TAG, "set video: "+ videoUri);
            mVideo.setImageBitmap(t);
            mVideo.setVisibility(View.VISIBLE);
            mVideoPlayBtn.setVisibility(View.VISIBLE);
            mVideo.setClickable(true);
            mVideo.setOnClickListener(new mediaClick(videoUri, videoType, ""));
        } else {
            mVideo.setVisibility(View.GONE);
        }

        String audioName = item.getAudioName();
        final Uri audioUri = item.getAudioUri();
        final String audioType = item.getAudioType();
        if (audioName != null) {
            MmsLog.i(TAG, "show audio name:" + audioName);
            mAudioName.setText(audioName);
            mAudioName.setTextSize(18);
            mAudioIcon.setVisibility(View.VISIBLE);
            mAudio.setVisibility(View.VISIBLE);
            mAudioName.setVisibility(View.VISIBLE);
            mAudioName.setClickable(true);
            mAudioName.setOnClickListener(new mediaClick(audioUri, audioType, audioName));
        } else {
            mAudioIcon.setVisibility(View.GONE);
            mAudioName.setVisibility(View.GONE);
            mAudio.setVisibility(View.GONE);
        }

        String text = item.getText();
        if ((imageUri != null || t != null) && text != null) {
            int leftAbs = Math.abs((item.getImageOrVideoLeft() - item.getTextLeft()));
            int topAbs = Math.abs(item.getImageOrVideoTop() - item.getTextTop());
            if (leftAbs > topAbs) {
                if (item.getImageOrVideoLeft() > item.getTextLeft()) {
                    mText = (TextView) itemView.findViewById(R.id.left_text);
                    if (item.getTextWidth() > 0) {
                        mText.setWidth(item.getTextWidth());
                    } else {
                        mText.setWidth(item.getImageOrVideoLeft());
                    }
                } else {
                    mText = (TextView) itemView.findViewById(R.id.right_text);
                    if (item.getTextWidth() > 0) {
                        mText.setWidth(item.getTextWidth());
                    } else {
                        mText.setWidth(item.getTextLeft());
                    }
                }
            } else if (item.getTextTop() > item.getImageOrVideoTop()) {
                mText = (TextView) itemView.findViewById(R.id.bottom_text);
            }
        }
        if (text != null) {
            mText.setText(parser.addSmileySpans(text));
            if(textSize != 0){
                mText.setTextSize(textSize);
            }
            mText.setVisibility(View.VISIBLE);
            /// M: ALPS00527989, Extend TextView URL handling @ {
            MmsConfig.setExtendUrlSpan(mText);
            /// @}
            itemView.setCurrentTextView(mText);
        } else {
            mText.setVisibility(View.GONE);
        }

        mListItemViewCache.put(arg0, itemView);
        return itemView;
    }

    public void clearAllCache() {
        if (mListItemViewCache.size() > 0) {
            View itemView = null;
            for (Integer key : mListItemViewCache.keySet()) {
                itemView = mListItemViewCache.get(key);
                EncapsulatedMTKImageView mImage = (EncapsulatedMTKImageView)itemView.findViewById(R.id.image);
                //MTKImageView mImage = (MTKImageView)itemView.findViewById(R.id.image);
                mImage.setImageURI(null);
            }
            mListItemViewCache.clear();
        }
    }

    /// M: new feature, tap picture,audio,video play in slideshow repview @{
    class mediaClick implements OnClickListener {
        private Uri mUri;
        private String mType;
        private String mTitle;

        public mediaClick(Uri uri, String type, String title) {
            mUri = uri;
            mType = type;
            mTitle = title;
        }

        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("SingleItemOnly", true);
            intent.putExtra("CanShare", false);
            intent.putExtra("title", mTitle);
            intent.setDataAndType(mUri, mType);
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Intent mchooserIntent = Intent.createChooser(intent, null);
                mContext.startActivity(mchooserIntent);
            }
        }
    };
    /// @}
}
