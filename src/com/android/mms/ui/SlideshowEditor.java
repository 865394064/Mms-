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

import com.mediatek.encapsulation.com.google.android.mms.EncapsulatedContentType;
import com.google.android.mms.MmsException;
import com.android.mms.model.AudioModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VideoModel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
/// M: Code analyze 001, new feature, import some useful classes @{
import android.text.TextUtils;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.MmsApp;
/// @}

import com.mediatek.mms.ext.IMmsAttachmentEnhance;
import com.mediatek.mms.ext.MmsAttachmentEnhanceImpl;
import com.mediatek.pluginmanager.PluginManager;
import com.mediatek.pluginmanager.Plugin;
import com.android.mms.model.SlideshowModel.MediaType;
import com.android.mms.MmsPluginManager;
/**
 * An utility to edit contents of a slide.
 */
public class SlideshowEditor {
    private static final String TAG = "Mms:slideshow";

    /// M: Code analyze 002, new feature, increase MAX_SLIDE_NUM to 20 @{
    public static final int MAX_SLIDE_NUM = 20;
    /// @}

    private final Context mContext;
    private SlideshowModel mModel;

    public SlideshowEditor(Context context, SlideshowModel model) {
        mContext = context;
        mModel = model;
    }

    public void setSlideshow(SlideshowModel model) {
        mModel = model;
    }

    /**
     * Add a new slide to the end of message.
     *
     * @return true if success, false if reach the max slide number.
     */
    public boolean addNewSlide() {
        int position = mModel.size();
        return addNewSlide(position);
    }

    /**
     * Add a new slide at the specified position in the message.
     *
     * @return true if success, false if reach the max slide number.
     * @throws IndexOutOfBoundsException - if position is out of range
     *         (position < 0 || position > size()).
     */
    public boolean addNewSlide(int position) {
        int size = mModel.size();
        if (size < MAX_SLIDE_NUM) {
            SlideModel slide = new SlideModel(mModel);

            TextModel text = new TextModel(
                    mContext, EncapsulatedContentType.TEXT_PLAIN, "text_" + size + ".txt",
                    mModel.getLayout().getTextRegion());
            /// M: Code analyze 003, new feature, add Exception handel @{
            try{
                    slide.add(text);
                    mModel.add(position, slide);
            }catch(ExceedMessageSizeException e){
                return false;
            }
            /// @}
            return true;
        } else {
            Log.w(TAG, "The limitation of the number of slides is reached.");
            return false;
        }
    }

    /**
     * Add an existing slide at the specified position in the message.
     *
     * @return true if success, false if reach the max slide number.
     * @throws IndexOutOfBoundsException - if position is out of range
     *         (position < 0 || position > size()).
     */
    public boolean addSlide(int position, SlideModel slide) {
        int size = mModel.size();
        if (size < MAX_SLIDE_NUM) {
            mModel.add(position, slide);
            return true;
        } else {
            Log.w(TAG, "The limitation of the number of slides is reached.");
            return false;
        }
    }

    /**
     * Remove one slide.
     *
     * @param position
     */
    public void removeSlide(int position) {
        mModel.remove(position);
    }

    /**
     * Remove all slides.
     */
    public void removeAllSlides() {
        while (mModel.size() > 0) {
            removeSlide(0);
        }
    }

    /**
     * Remove the text of the specified slide.
     *
     * @param position index of the slide
     * @return true if success, false if no text in the slide.
     */
    public boolean removeText(int position) {
        return mModel.get(position).removeText();
    }

    public boolean removeImage(int position) {
        return mModel.get(position).removeImage();
    }

    public boolean removeVideo(int position) {
        return mModel.get(position).removeVideo();
    }

    public boolean removeAudio(int position) {
        return mModel.get(position).removeAudio();
    }

    public void changeText(int position, String newText) {
        if (newText != null) {
            SlideModel slide = mModel.get(position);
            TextModel text = slide.getText();
            /// M: Code analyze 004, new feature, must check MessageSize if change text @{
            int oldLength ;
            int newLength = newText.getBytes().length;
            if (text == null) {
                oldLength = 0;
                text = new TextModel(mContext,
                        EncapsulatedContentType.TEXT_PLAIN, "text_" + position + ".txt",
                        mModel.getLayout().getTextRegion(),
                        TextUtils.getTrimmedLength(newText)>=0 ? newText.getBytes() : null);
//                text.setText(newText);
                text.setTextWithoutNotify(newText);
                slide.add(text);
            } else if (!newText.equals(text.getText())) {
                oldLength = text.getText().getBytes().length;
                /// M: fix bug ALPS00500614, solute shake when delete text and appear301kb. @{
                if (oldLength == 0) {
                    newLength += MediaModel.getSlideSmileSize();
                } else if (newText.length() == 0) {
                    oldLength += MediaModel.getSlideSmileSize();
                }
                /// @}
                if (newLength > oldLength) {
                    mModel.checkMessageSize(newLength - oldLength);
                    mModel.increaseSlideshowSize(newLength - oldLength);
                    text.setText(newText);
                } else {
                    mModel.decreaseSlideshowSize(oldLength - newLength);
                    text.setTextWithoutNotify(newText);
                }
//                text.setText(newText);
                text.changeSizeOnlyForText(newText);
            }
            /// @}
        }
    }

    public void changeImage(int position, Uri newImage) throws MmsException {
        /// M: Code analyze 005, fix bug ALPS00236562, can't add image when remove slideshow @{
        if (mModel.get(position) != null) {
            /// M: Code analyze 006, fix bug ALPS00334746,
            /// must remove old thumbnail if replace image/video successfully @{
            Uri preUri = null;
            SlideModel sm = mModel.get(position);
            if (sm.hasImage()) {
                preUri = sm.getImage().getUri();
            }
            ImageModel mImageModel =  new ImageModel(mContext, newImage, mModel.getLayout()
                            .getImageRegion());

            IMmsAttachmentEnhance mMmsAttachmentEnhancePlugin = 
                  (IMmsAttachmentEnhance)MmsPluginManager.getMmsPluginObject(MmsPluginManager.MMS_PLUGIN_TYPE_MMS_ATTACHMENT_ENHANCE);
            //add for attachment enhance
            if (mMmsAttachmentEnhancePlugin != null) {
                  if (mMmsAttachmentEnhancePlugin.isSupportAttachmentEnhance() == true) {
                    String[] fileNames = mModel.getAllMediaNames(MediaType.IMAGE);
                    String mSrc = MessageUtils.getUniqueName(fileNames, mImageModel.getSrc());
                    mImageModel.setSrc(mSrc);
                  }
            }
            mModel.get(position).add(mImageModel
                /*new ImageModel(mContext, newImage, mModel.getLayout().getImageRegion())*/);
            if (preUri != null) {
                MmsApp.getApplication().getThumbnailManager().removeThumbnail(preUri);
            }
            /// @}
        }
        /// @}
    }

    public void changeAudio(int position, Uri newAudio) throws MmsException {
        AudioModel audio = new AudioModel(mContext, newAudio);
        SlideModel slide = mModel.get(position);
        IMmsAttachmentEnhance mMmsAttachmentEnhancePlugin = 
                  (IMmsAttachmentEnhance)MmsPluginManager.getMmsPluginObject(MmsPluginManager.MMS_PLUGIN_TYPE_MMS_ATTACHMENT_ENHANCE);

        //add for attachment enhance
        if (mMmsAttachmentEnhancePlugin != null) {
              if (mMmsAttachmentEnhancePlugin.isSupportAttachmentEnhance() == true) {
                //add for attachment enhance
                String[] fileNames = mModel.getAllMediaNames(MediaType.AUDIO);
                String mSrc = MessageUtils.getUniqueName(fileNames, audio.getSrc());
                audio.setSrc(mSrc);
              }
        }
        slide.add(audio);
        slide.updateDuration(audio.getDuration());
    }

    public void changeVideo(int position, Uri newVideo) throws MmsException {
        VideoModel video = new VideoModel(mContext, newVideo,
                mModel.getLayout().getImageRegion());
        if (mModel.get(position) != null) {
            SlideModel slide = mModel.get(position);
            /// M: Code analyze 006, fix bug ALPS00334746,
            /// must remove old thumbnail if replace image/video successfully @{
            Uri preUri = null;
            if (slide.hasVideo()) {
                preUri = slide.getVideo().getUri();
            }
        //add for attachment enhance

        IMmsAttachmentEnhance mMmsAttachmentEnhancePlugin = 
                  (IMmsAttachmentEnhance)MmsPluginManager.getMmsPluginObject(MmsPluginManager.MMS_PLUGIN_TYPE_MMS_ATTACHMENT_ENHANCE);
        
        if (mMmsAttachmentEnhancePlugin != null) {
              if (mMmsAttachmentEnhancePlugin.isSupportAttachmentEnhance() == true) {
                //add for attachment enhance
                String[] fileNames = mModel.getAllMediaNames(MediaType.VIDEO);
                String mSrc = MessageUtils.getUniqueName(fileNames, video.getSrc());
                video.setSrc(mSrc);
              }
        }
            slide.add(video);
            slide.updateDuration(video.getDuration());
            if (preUri != null) {
                MmsApp.getApplication().getThumbnailManager().removeThumbnail(preUri);
            }
        }
        /// @}
    }

    public void moveSlideUp(int position) {
        /// M: Code analyze 007, fix bug ALPS00243328, should increase size,
        /// because remove slide and decrease size when move slide position @{
        mModel.addNoCheckSize(position - 1, mModel.remove(position));
        /// @}
    }

    public void moveSlideDown(int position) {
        /// M: Code analyze 007, fix bug ALPS00243328, should increase size,
        /// because remove slide and decrease size when move slide position @{
        mModel.addNoCheckSize(position + 1, mModel.remove(position));
        /// @}
    }

    public void changeDuration(int position, int dur) {
        if (dur >= 0) {
            mModel.get(position).setDuration(dur);
        }
    }

    public void changeLayout(int layout) {
        mModel.getLayout().changeTo(layout);
    }

    public RegionModel getImageRegion() {
        return mModel.getLayout().getImageRegion();
    }

    public RegionModel getTextRegion() {
        return mModel.getLayout().getTextRegion();
    }

    /// M: Fix bug ALPS00450387
    public String getSlideTextOf(int position) {
        TextModel textModel = mModel.get(position).getText();
        return textModel == null ? "" : textModel.getText();
    }
}
