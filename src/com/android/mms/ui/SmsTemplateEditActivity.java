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

import static android.provider.BaseColumns._ID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.os.Bundle;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms;
import android.text.InputFilter;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.mms.R;
import com.android.mms.MmsConfig;
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony;

import java.util.ArrayList;
import java.util.List;

//add by xujia
import android.view.View.*;

/** M:
 * SmsTemplateEditActivity
 */
public class SmsTemplateEditActivity extends Activity {

    private static String TAG = "MMS/SmsTempalteEditor";
    private static String TEXT = "text";
    private static int QUICK_TEXT_HAS_ALREADY = -1;
    private static int QUICK_TEXT_NULL = -2;

    private Integer mQuickTextId;
    private Integer mMaxQuickTextId;
    private String mQuickText;

    @SuppressWarnings("unused")
    private AlertDialog mQuicktextAlertDialog;
    private ListView mListView;
    private Button addButton;
    private EditText mNewQuickText;
    private EditText mOldQuickText;
    @SuppressWarnings("unused")
    private TextView textItem;

    private ArrayAdapter<String> adapter;
    private List<Integer> allQuickTextIds = new ArrayList<Integer>();
    private List<String> allQuickTexts = new ArrayList<String>();
    private final int MAX_EDITABLE_LENGTH = 128;
    private Toast mToast;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_template_edit_activity);

//add by xujia 20130826
        
        setBackButtonOnClickListener(new android.view.View.OnClickListener() {

            public void onClick(View v) {
                finish();
            }
            
        });
        //end by xujia
        //getActionBar().setDisplayHomeAsUpEnabled(true);//disabled by xujia 20130826

        mToast = Toast.makeText(this, R.string.cannot_save_message, Toast.LENGTH_SHORT);
        addButton = (Button) findViewById(R.id.quickText_add_button);
        addButton.setTextSize(17);
        addButton.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.quickText_add_button:
                    addQuickText();
                    break;
                default:
                    break;
                }
            }
        });

        mListView = (ListView) findViewById(R.id.quick_text_list);
        mListView.setOnItemClickListener(new OnItemClickListener () {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                InputMethodManager inputM =
                    (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(getWindow() != null && getWindow().getCurrentFocus() != null) {
                    inputM.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                }
                mQuickTextId = allQuickTextIds.get(Integer.valueOf((int) id));
                mQuickText = allQuickTexts.get(Integer.valueOf((int) id));
                showEditDialog();
                return;
            }    
        });
        mNewQuickText = (EditText) findViewById(R.id.new_quick_text);
        mNewQuickText.setHint(R.string.type_to_quick_text);
        textItem = (TextView) findViewById(R.id.new_quick_text);

        /// M: new feature, add default quick_text @{
        if (MmsConfig.getInitQuickText()) {
            mMaxQuickTextId = 0;
            Cursor cursor = getSTs();
            allQuickTextIds.clear();
            allQuickTexts.clear();
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        int qtId = cursor.getInt(0);
                        allQuickTextIds.add(qtId);
                        allQuickTexts.add(cursor.getString(1));
                        mMaxQuickTextId = mMaxQuickTextId >= qtId? mMaxQuickTextId : qtId;
                    }
                } finally {
                    cursor.close();
                }
            }

            String[] default_quick_texts = getResources().getStringArray(R.array.default_quick_texts);
            for (int i = 0; i < default_quick_texts.length; i++) {
                String string = default_quick_texts[i];
                addST(string);
            }
            MmsConfig.setPreQuickText(default_quick_texts);
            MmsConfig.setInitQuickText(false);
        }
        /// @}

        updateAllQuicktexts();
    }

    private void showEditDialog() {
        mQuicktextAlertDialog = new AlertDialog.Builder(this)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setTitle(R.string.quick_text_editor)
        .setMessage(mQuickText)
        .setPositiveButton(R.string.edit, new EditButtonListener())
        .setNeutralButton(R.string.delete, new DeleteButtonListener())
        .setNegativeButton(android.R.string.cancel, new CancelButtonListener())
        .show();
    }

    // 4ButtonListener
    private class EditButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            showEditDialog(mQuickText);
        }
    }

    private class UpdateButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            String newQuicktext = mOldQuickText.getText().toString();
            int i = updateST(mQuickTextId, newQuicktext);
            dialog.dismiss();
            if (i > 0) {
                mQuickTextId = null;
                updateAllQuicktexts();
                makeAToast(R.string.modify_successful);
            } else if (i == QUICK_TEXT_HAS_ALREADY) {
                makeAToast(R.string.already_have_quick_text);
            } else if (i == QUICK_TEXT_NULL) {
                makeAToast(R.string.cannot_save_message);
                showEditDialog(mQuickText);
            } else {
                makeAToast(R.string.modify_unsuccessful);
            }
        }
    }

    private class DeleteButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            int i = delST(mQuickTextId);
            updateAllQuicktexts();
            dialog.dismiss();
            if (i > 0) {
                mQuickTextId = null;
                makeAToast(R.string.delete_successful);
            } else {
                makeAToast(R.string.delete_unsuccessful);
            }
        }
    }

    private class CancelButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    private void addQuickText() {
        String currentText = mNewQuickText.getText().toString().trim();
        if (currentText.equals("")) {
            makeAToast(R.string.cannot_save_message);
            return;
        }
        if (currentText.length() != 0) {
            if (addST(currentText)) {
                mNewQuickText.setText("");
                updateAllQuicktexts();
                makeAToast(getString(R.string.add_quick_text_successful) + " : \n" + currentText);
            } else {
                makeAToast(R.string.already_have_quick_text);
            }
        } else {
            mToast.show();
        }
        return;
    }

    private boolean addST(String str) {
        // Insert a new record into the Events data source.
        // You would do something similar for delete and update.
        if (hasQuicktext(str)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(_ID, mMaxQuickTextId + 1);
        values.put(TEXT, str);
        getContentResolver().insert(EncapsulatedTelephony.MmsSms.CONTENT_URI_QUICKTEXT, values);
        if (MmsConfig.getInitQuickText()) {
            mMaxQuickTextId++;
        }
        return true;
    }

    private int delST(Integer id) {
        return getContentResolver().delete(EncapsulatedTelephony.MmsSms.CONTENT_URI_QUICKTEXT, _ID + "=" + id, null);
    }
    
    private int updateST(Integer id, String text) {
        if (text.trim().equals("")) {
            return QUICK_TEXT_NULL;
        } else if (hasQuicktext(text)) {
            return QUICK_TEXT_HAS_ALREADY;
        }
        ContentValues values = new ContentValues();
        values.put(_ID, id);
        values.put(TEXT, text);
        return getContentResolver().update(EncapsulatedTelephony.MmsSms.CONTENT_URI_QUICKTEXT, values, _ID + "=" + id, null);
    }

    private Cursor getSTs() {
        // Perform a managed query. The Activity will handle closing
        // and re-querying the cursor when needed.
        Cursor cursor = getContentResolver().query(EncapsulatedTelephony.MmsSms.CONTENT_URI_QUICKTEXT, null, null, null, null);
        /// M: fix bug ALPS00450001, avoiding cursor leak
        // startManagingCursor(cursor);
        return cursor;
    }
    
    
    private boolean hasQuicktext(String str) {
        for (String s : allQuickTexts) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private void updateAllQuicktexts() {
        mMaxQuickTextId = 0;
        Cursor cursor = getSTs();
        allQuickTextIds.clear();
        allQuickTexts.clear();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int qtId = cursor.getInt(0);
                    allQuickTextIds.add(qtId);
                    allQuickTexts.add(cursor.getString(1));
                    mMaxQuickTextId = mMaxQuickTextId >= qtId? mMaxQuickTextId : qtId;
                }
            } finally {
                cursor.close();
            }
        }
        adapter = new ArrayAdapter<String>(this, R.layout.quick_text_edit_item, allQuickTexts);
        mListView.setAdapter(adapter);
    }

    private void makeAToast(Integer strId){
        if (strId != null) {
            Toast.makeText(this, strId, Toast.LENGTH_SHORT).show();
        }
    }

    private void makeAToast(String message){
        if (message != null && !message.equals("")) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void showEditDialog(String quickText) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SmsTemplateEditActivity.this);
        mOldQuickText = new EditText(dialog.getContext());
        mOldQuickText.setHint(R.string.type_to_quick_text);
        mOldQuickText.computeScroll();
        mOldQuickText.setText(quickText);
        mOldQuickText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
        mOldQuickText.setMaxLines(4);
        mQuicktextAlertDialog = dialog
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setTitle(R.string.quick_text_editor)
        .setView(mOldQuickText)
        .setPositiveButton(android.R.string.ok, new UpdateButtonListener())
        .setNegativeButton(android.R.string.cancel, new CancelButtonListener())
        .show();
    }

    //begin add by xujia 20130826
        public void setBackButtonOnClickListener(android.view.View.OnClickListener onClickListener) {
        View v = findViewById(R.id.back_settings_btn);
        if (v != null) {
            v.setOnClickListener(onClickListener);
        }
    }//end by xujia
    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
}
