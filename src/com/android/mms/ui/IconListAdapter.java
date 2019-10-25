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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.android.mms.R;

/**
 * An adapter to store icons.
 */
public class IconListAdapter extends ArrayAdapter<IconListAdapter.IconListItem> {
    protected LayoutInflater mInflater;
    private static final int mResource = R.layout.icon_list_item;
    private Context mContext;//added by xiashuaishuai on  20141224 for Bug[3294]
    public IconListAdapter(Context context,
            List<IconListItem> items) {
        super(context, mResource, items);
		this.mContext=context;//added by xiashuaishuai on  20141224 for Bug[3294]
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView text;
        //ageView image;

        View view;
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        // Set text field
        text = (TextView) view.findViewById(R.id.text1);
        text.setText(getItem(position).getTitle());
        text.setTextColor(mContext.getResources().getColor(R.drawable.iphone_blue_1));//added by xiashuaishuai on  20141224 for Bug[3294]
        // Set resource icon
        /*image = (ImageView) view.findViewById(R.id.icon);
       mage.setImageResource(getItem(position).getResource());*///Mody by xiashuaishuai on 20150115 for Bug[3294]

        return view;
    }

    public static class IconListItem {
        private final String mTitle;
        //Begin:Mody by xiashuaishuai 20141218 for Bug[3294]	
        //private final int mResource;
        public IconListItem(String title/**, int resource**/) {
            //mResource = resource;
            mTitle = title;
        }		 
        public String getTitle() {
            return mTitle;
        }

        /*blic int getResource() {
            return mResource;
        }*///nd; :Mody by xiashuaishuai 20141218 for Bug[3294]	 /
    }
}
