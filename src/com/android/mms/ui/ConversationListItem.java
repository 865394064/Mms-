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

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.util.SmileyParser;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

/// M:
import android.provider.Telephony.Mms;
import android.provider.Telephony.Threads;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.mms.data.CBMessage;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.mediatek.encapsulation.com.mediatek.common.featureoption.EncapsulatedFeatureOption;
import com.mediatek.encapsulation.MmsLog;
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony;
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony.SIMInfo;
import com.mediatek.encapsulation.com.android.internal.telephony.EncapsulatedPhone;

/// M: add for ipmessage
import com.mediatek.ipmsg.util.IpMessageUtils;
import com.android.mms.MmsConfig;
import com.mediatek.mms.ipmessage.IpMessageConsts;
import com.mediatek.mms.ipmessage.IpMessageConsts.IpMessageServiceId;
import android.widget.RelativeLayout;
import com.android.mms.ui.widget.SmsLeftYuan;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import com.android.mms.anim.AnimationRotateZ;
import com.android.mms.anim.AnimationRotateZResume;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.animation.AnimatorSet;
import android.view.animation.AccelerateInterpolator;
import android.animation.Animator.AnimatorListener;
import android.animation.Animator;

import com.mediatek.common.featureoption.FeatureOption;
/**
 * This class manages the view for given conversation.
 */
public class ConversationListItem extends RelativeLayout implements Contact.UpdateListener,View.OnClickListener,
            Checkable {
    private static final String TAG = "sjb";
    private static final boolean DEBUG = false;

    private TextView mSubjectView;
    private TextView mFromView;
    private TextView mDateView;
   // private View mAttachmentView;
    //private ImageView mErrorIndicator;
    //private QuickContactBadge mAvatarView;

    private static Drawable sDefaultContactImage;

    // For posting UI update Runnables from other threads:
    private Handler mHandler = new Handler();

    private Conversation mConversation;

    public static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    /// M: Code analyze 028, For new feature, unread icon is shown top right corner . @{
    private ImageView mPresenceView;
    /// @}
    private ImageView mUnreadIcon;
    private Context mContext;
    /// M: add for new common feature.
    private View mMuteView;
    private TextView mUnreadView;
    private static final int MAX_UNREAD_MESSAGES_COUNT = 999;
    private static final String MAX_UNREAD_MESSAGES_STRING = "999+";
    private static final int MAX_READ_MESSAGES_COUNT = 9999;
    private static final String MAX_READ_MESSAGES_STRING = "9999+";
    private boolean mSubjectSingleLine;
    private RelativeLayout mSmsContentRow;
    protected ImageView mMinus;
    protected ImageView mBtnTurnTo;
    private ImageView mImgarror;
    protected View mDelete;
    private static long DURATION = 160;
    private static final int BUTTON_SMSLEFT_ROTATE_TRUE = 1;
    private static final int BUTTON_SMSLEFT_ROTATE_FALSE = 2;
    private static final int BUTTON_DELETE = 3;
    private DeleteItemOfSmsListListener mDeleteItemOfSmsListListener = null;
    public ConversationListItem(Context context) {
        super(context);
        mContext = context;
    }

    public ConversationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
	Log.v(TAG,"onFinishInflate---");
        mFromView = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);
        /// M: Code analyze 028, For new feature, unread icon is shown top right corner . @{
        mPresenceView = (ImageView) findViewById(R.id.presence);
        /// @}
        mUnreadIcon = (ImageView) findViewById(R.id.unread_icon);//added by lzp for bug [2537]
        mDateView = (TextView) findViewById(R.id.date);
        //mAttachmentView = findViewById(R.id.attachment);
        //mErrorIndicator = (ImageView)findViewById(R.id.error);
        //mAvatarView = (QuickContactBadge) findViewById(R.id.avatar);
        mSmsContentRow = (RelativeLayout) findViewById(R.id.mms_item_layout);
	 mMinus =  (ImageView) this.findViewById(R.id.img_mms_item_mius);
	 mMinus.setVisibility(View.INVISIBLE);
	 mMinus.setTag(BUTTON_SMSLEFT_ROTATE_FALSE);
	 mMinus.setOnClickListener(this);
        mBtnTurnTo = (ImageView) this.findViewById(R.id.imgarror);
	 mDelete = this.findViewById(R.id.btn_mms_item_delete);
	 mDelete.setTag(BUTTON_DELETE);
	 mDelete.setOnClickListener(this);
	 mImgarror = (ImageView) this.findViewById(R.id.imgarror);
    }

    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Only used for header binding.
     */
    public void bind(String title, String explain) {
        Log.v(TAG,"bind---");
        mFromView.setText(title);
        mSubjectView.setText(explain);
    }

    private CharSequence formatMessage() {
        final int color = android.R.styleable.Theme_textColorSecondary;
        /// M: Code analyze 029, For new feature ALPS00111828, add CellBroadcast feature . @{
        String from = null;
        if (mConversation.getType() == EncapsulatedTelephony.Threads.CELL_BROADCAST_THREAD) {
            int channelId = 0;
            MmsLog.e(TAG, "recipients = " + mConversation.getRecipients().formatNames(", "));
            String number = parseNumberForCb(mConversation.getRecipients().formatNames(", "));
            if (!TextUtils.isEmpty(number)) {
                try {
                    channelId = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    MmsLog.e(TAG, "format number error!");
                }
            }

            String name = null;
            SIMInfo info = SIMInfo.getSIMInfoBySlot(getContext(), EncapsulatedPhone.GEMINI_SIM_1);
            if (info != null) {
                name = CBMessage.getCBChannelNameGemini(channelId, (int) info.getSimId());
                if (name == MmsApp.getApplication().getApplicationContext()
                        .getString(R.string.cb_default_channel_name)) {
                    info = SIMInfo.getSIMInfoBySlot(getContext(), EncapsulatedPhone.GEMINI_SIM_2);
                    if (info != null) {
                        name = CBMessage.getCBChannelNameGemini(channelId, (int) info.getSimId());
                    }
                }
            } else {
                info = SIMInfo.getSIMInfoBySlot(getContext(), EncapsulatedPhone.GEMINI_SIM_2);
                if (info != null) {
                    name = CBMessage.getCBChannelNameGemini(channelId, (int) info.getSimId());
                }
            }

            if (TextUtils.isEmpty(name)) {
                name = MmsApp.getApplication().getApplicationContext()
                        .getString(R.string.cb_default_channel_name);
            }
            try {
                from = name + "("
                        + Integer.parseInt(parseNumberForCb(mConversation.getRecipients()
                                .formatNames(", "))) + ")";
            } catch (NumberFormatException e) {
                MmsLog.e(TAG, "format recipient number error!");
            }
        } else {
            /// M: add for ip message
            if (MmsConfig.getIpMessagServiceId(mContext) == IpMessageServiceId.ISMS_SERVICE
                    && mConversation.getRecipients().size() == 1) {
                Contact contact = mConversation.getRecipients().get(0);
                MmsLog.d("avatar", "ConvListItem.formatMessage(): number = " + contact.getNumber()
                    + ", name = " + contact.getName());
                if (contact.getNumber().startsWith(IpMessageConsts.GROUP_START)) {
                    if (mConversation.getThreadId() == 0) {
                        contact.setThreadId(mConversation.getThreadId());
                    }
                    if (TextUtils.isEmpty(contact.getName()) || contact.getNumber().equals(contact.getName())) {
                        contact.setName(
                            IpMessageUtils.getContactManager(mContext).getNameByThreadId(mConversation.getThreadId()));
                    }
                    MmsLog.d("avatar", "ConvListItem.formatMessage(): number = " + contact.getNumber()
                        + ", group name = " + contact.getName());
                }
            }
            from = mConversation.getRecipients().formatNames(", ");
        }

        if (TextUtils.isEmpty(from)) {
            from = mContext.getString(android.R.string.unknownName);
        }
        /// @}

        SpannableStringBuilder buf = new SpannableStringBuilder(from);

        /// M:
        /*int before = buf.length();
        if (!mConversation.hasUnreadMessages()) {
            if (mConversation.getMessageCount() > 1) {
                MmsLog.d("unread", "formatMessage(): Thread " + mConversation.getThreadId() + " has no unread message.");
                int count = mConversation.getMessageCount();
                if (count > MAX_READ_MESSAGES_COUNT) {
                    buf.append("  " + MAX_READ_MESSAGES_STRING);
                } else {
                    buf.append("  " + count);
                }
                buf.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.message_count_color)),
                        before, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }*/
/** M: Remove Google default code
        if (mConversation.hasDraft()) {
           // buf.append(mContext.getResources().getString(R.string.draft_separator));
            int before = buf.length();
            int size;
            buf.append(",  " + mContext.getResources().getString(R.string.has_draft));
            size = android.R.style.TextAppearance_Small;
            buf.setSpan(new TextAppearanceSpan(mContext, size, color), before + 1,
                    buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            buf.setSpan(new ForegroundColorSpan(
                    mContext.getResources().getColor(R.drawable.text_color_red)),
                    before + 1, buf.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

 */
        // Unread messages are shown in bold
        /**Begin: mod by lzp for bug [2537] **/
        if (mConversation.hasUnreadMessages()) {
            /*buf.setSpan(STYLE_BOLD, 0, buf.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);*/
        	if(mUnreadIcon != null)
        		mUnreadIcon.setVisibility(View.VISIBLE);
        } else {
        	if(mUnreadIcon != null)
        		mUnreadIcon.setVisibility(View.INVISIBLE);
        }
        /**End: mod by lzp for bug [2537] **/
        return buf;
    }

/*
    private void updateAvatarView() {
        Drawable avatarDrawable;
        /// M:
        mAvatarView.setGroupAvator(false);
        if (mConversation.getRecipients().size() == 1) {
            Contact contact = mConversation.getRecipients().get(0);
            /// M: add for ipmessage
            boolean isGroup = contact.getNumber().startsWith(IpMessageConsts.GROUP_START);
            MmsLog.d("avatar", "ConvListItem.updateAvatarView(): isGroup = " + isGroup +
                                ", number = " + contact.getNumber());
            if (isGroup) {
                MmsLog.d("avatar", "ConvListItem.updateAvatarView(): get avatart by threadId, threadId = "
                    + mConversation.getThreadId());
                avatarDrawable = contact.getGroupAvatar(mContext, mConversation.getThreadId());
                MmsLog.d("avatar", "ConvListItem.updateAvatarView(): bitmap is null ?= " + (null == avatarDrawable));
                if (null != avatarDrawable) {
                    MmsLog.d("avatar", "ConvListItem.updateAvatarView(): set group avatar.");
                    mAvatarView.setImageDrawable(avatarDrawable);
                    mAvatarView.setVisibility(View.VISIBLE);
                    mAvatarView.setThreadId(mConversation.getThreadId());
                    mAvatarView.setGroupAvator(true);
                    /// M: assign make group can click.
                    mAvatarView.assignContactUri(contact.getUri());
                    return;
                }
            }
            avatarDrawable = contact.getAvatar(mContext, sDefaultContactImage, mConversation.getThreadId());

            /// M: fix bug ALPS00400483, same as 319320, clear all data of mAvatarView firstly. @{
            mAvatarView.assignContactUri(null);
            /// @}

            /// M: Code analyze 030, For new feature ALPS00241750, Add email address
            /// to email part in contact . @{
            String number = contact.getNumber();
            if (Mms.isEmailAddress(number)) {
                mAvatarView.assignContactFromEmail(number, true);
            } else {       
                if (contact.existsInDatabase()) {
                    mAvatarView.assignContactUri(contact.getUri());
                } else {
                    mAvatarView.assignContactFromPhone(number, true);
                }
                /// @}
            }
        } else {
            // TODO get a multiple recipients asset (or do something else)
            avatarDrawable = sDefaultContactImage;
            mAvatarView.assignContactUri(null);
        }
        mAvatarView.setImageDrawable(avatarDrawable);
        mAvatarView.setVisibility(View.VISIBLE);
    }
*/
    private void updateFromView() {
        mFromView.setText(formatMessage());
        //updateAvatarView();
    }

    private Runnable mUpdateFromViewRunnable = new Runnable() {
        public void run() {
            updateFromView();
        }
    };

    public void onUpdate(Contact updated) {
        //if (Log.isLoggable(LogTag.CONTACT, Log.DEBUG)) {
        Log.v(TAG, "onUpdate: " + this + " contact: " + updated);
        //}
        /// M: fix blank screen issue. if there are 1000 threads, 1 recipient each thread,
        /// and 8 list items in each screen, onUpdate() will be called 8000 times.
        /// mUpdateFromViewRunnable run in UI thread will blocking the other things.
        /// remove blocked mUpdateFromViewRunnable.
        mHandler.removeCallbacks(mUpdateFromViewRunnable);
        mHandler.post(mUpdateFromViewRunnable);
    }

    public final void bind(Context context, final Conversation conversation) {
        //if (DEBUG) Log.v(TAG, "bind()");
        Log.v(TAG,"bind---2");
        mConversation = conversation;

        //updateBackground();
/*
        /// M: update read view
        if (mConversation.hasUnreadMessages()) {
            mPresenceView.setVisibility(View.VISIBLE);
            int unreadCount = mConversation.getUnreadMessageCount();
            String unreadString = null;
            if (unreadCount > MAX_UNREAD_MESSAGES_COUNT) {
                unreadString = MAX_UNREAD_MESSAGES_STRING;
            } else {
                unreadString = "" + mConversation.getUnreadMessageCount();
            }
            mUnreadView.setText(unreadString);
            mUnreadView.setVisibility(View.VISIBLE);
        } else {
            mPresenceView.setVisibility(View.INVISIBLE);
            mUnreadView.setVisibility(View.GONE);
        }
*/
//        LayoutParams attachmentLayout = (LayoutParams)mAttachmentView.getLayoutParams();
        boolean hasError = conversation.hasError();
        // When there's an error icon, the attachment icon is left of the error icon.
        // When there is not an error icon, the attachment icon is left of the date text.
        // As far as I know, there's no way to specify that relationship in xml.
        /// M @{
        // if (hasError) {
        //     attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.error);
        // } else {
        //     attachmentLayout.addRule(RelativeLayout.LEFT_OF, R.id.date);
        // }
        /// @}

        boolean hasAttachment = conversation.hasAttachment();
        //mAttachmentView.setVisibility(hasAttachment ? VISIBLE : GONE);

        /// M: Code analyze 031, For bug ALPS00235723, The crolling performance of message . @{
        // Date
        mDateView.setVisibility(VISIBLE);
        /// M:
        if (ConversationList.sConversationListOption == ConversationList.OPTION_CONVERSATION_LIST_IMPORTANT &&
            !conversation.hasUnreadMessages()) {
            mDateView.setText(MessageUtils.formatTimeStampStringExtend(context, conversation.getImpDate()));
        } else {
            mDateView.setText(MessageUtils.formatTimeStampStringExtend(context, conversation.getDate()));
        }

        // From.
        mFromView.setVisibility(VISIBLE);
        if (mConversation.getType() == Threads.IP_MESSAGE_GUIDE_THREAD) {
            // this is ipmessage guide thread
            mFromView.setText(IpMessageUtils.getResourceManager(context)
                .getSingleString(IpMessageConsts.string.ipmsg_service_title));
        } else {
            mFromView.setText(formatMessage());
        /// @}

        /// M: this local variable has never been used. delete google default code.
        // Register for updates in changes of any of the contacts in this conversation.
        // ContactList contacts = conversation.getRecipients();

            if (DEBUG) {
                Log.v(TAG, "bind: contacts.addListeners " + this);
            }
            Contact.addListener(this);
        }

        // Subject
//        SmileyParser parser = SmileyParser.getInstance();
//        mSubjectView.setText(parser.addSmileySpans(conversation.getSnippet()));

        if (mSubjectSingleLine) {
            mSubjectView.setSingleLine(true);
        }
        /// M: Code analyze 031, For bug ALPS00235723, The crolling performance of message . @{
        mSubjectView.setVisibility(VISIBLE);
        /// @}
        /// M:
        if (mConversation.getTyping()) {
            mSubjectView.setText(IpMessageUtils.getResourceManager(context)
                .getSingleString(IpMessageConsts.string.ipmsg_typing));
        } else if (mConversation.getType() == Threads.IP_MESSAGE_GUIDE_THREAD) {
            mSubjectView.setText(IpMessageUtils.getResourceManager(context)
                .getSingleString(IpMessageConsts.string.ipmsg_introduction));
        } else if (ConversationList.sConversationListOption == ConversationList.OPTION_CONVERSATION_LIST_IMPORTANT &&
            !conversation.hasUnreadMessages()) {
            mSubjectView.setText(conversation.getImpSnippet());
        } else {
            mSubjectView.setText(conversation.getSnippet());
        }
 //       LayoutParams subjectLayout = (LayoutParams)mSubjectView.getLayoutParams();
 //       // We have to make the subject left of whatever optional items are shown on the right.
 //       subjectLayout.addRule(RelativeLayout.LEFT_OF, hasAttachment ? R.id.attachment :
  //          (hasError ? R.id.error : R.id.date));

        // Transmission error indicator.
        /// M: Code analyze 032, For new feature ALPS00347707, add for wap push error icon . @{
        if (EncapsulatedFeatureOption.MTK_WAPPUSH_SUPPORT && hasError) {
            if (conversation.getType() == EncapsulatedTelephony.Threads.WAPPUSH_THREAD) {
                //mErrorIndicator.setImageResource(R.drawable.alert_wappush_si_expired);
            } else {
                //mErrorIndicator.setImageResource(R.drawable.ic_list_alert_sms_failed);
            }
        }
        /// @}
        //mErrorIndicator.setVisibility(hasError ? VISIBLE : GONE);
/*
        /// M: add for ipmessage
        if (mConversation.getType() == Threads.IP_MESSAGE_GUIDE_THREAD) {
            mAvatarView.assignContactUri(null);
            //Drawable image = context.getResources().getDrawable(R.drawable.ipmsg_service);
            Drawable image = IpMessageUtils.getResourceManager(context)
                                        .getSingleDrawable(IpMessageConsts.drawable.ipmsg_service);
            mAvatarView.setImageDrawable(image);
            mAvatarView.setVisibility(View.VISIBLE);
        } else {
            updateAvatarView();
            /// M:
            mMuteView.setVisibility(View.GONE);
            if (conversation.isMute()) {
                mMuteView.setVisibility(View.VISIBLE);
            }
        }

        ///M: add for cmcc, show draft icon when conversation has draft.  @{
        if (MmsConfig.isShowDraftIcon()) {
            View draftIcon = findViewById(R.id.draft);
            if (draftIcon != null) {
                if (mConversation.hasDraft()) {
                    draftIcon.setVisibility(View.VISIBLE);
                } else {
                    draftIcon.setVisibility(View.GONE);
                }
            }
        }*/
        ///}@
    }



    public final void unbind() {
        if (DEBUG) {
            Log.v(TAG, "unbind: contacts.removeListeners " + this);
        }
        // Unregister contact update callbacks.
        Contact.removeListener(this);
    }
    public void setChecked(boolean checked) {
        mConversation.setIsChecked(checked);

    }

    public boolean isChecked() {
        return mConversation.isChecked();
    }

    public void toggle() {
        mConversation.setIsChecked(!mConversation.isChecked());
    }

    /// M: Code analyze 031, For bug ALPS00235723, The crolling performance of message . @{
    public void bindDefault() {
        MmsLog.d(TAG, "bindDefault().");
        mPresenceView.setVisibility(GONE);
        mUnreadIcon.setVisibility(View.INVISIBLE);
        //mAttachmentView.setVisibility(GONE);
        mDateView.setVisibility(View.GONE);
        mFromView.setText(R.string.refreshing);
        mSubjectView.setVisibility(GONE);
        //mErrorIndicator.setVisibility(GONE);
        //mAvatarView.setImageDrawable(sDefaultContactImage);
    }
    /// @}

    /// M: Code analyze 029, For new feature ALPS00111828, add CellBroadcast feature . @{
    private  String parseNumberForCb(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();

        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
    /// @}

    public void setSubjectSingleLineMode(boolean value) {
        mSubjectSingleLine = value;
    }

    /// M: Make sure listeners are removed so that ConversationList instance can be released @{
    @Override
    protected void onDetachedFromWindow() {
        Log.v(TAG, "onDetachedFromWindow!!!");
        super.onDetachedFromWindow();
        Contact.removeListener(this);
    }
    /// @}
   public void changeStateToEdit(){
	   mUnreadIcon.setAlpha(0f);//added by lzp for bug [2605]
   Log.v("sjb","changeStateToEdit------");
		ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x", FeatureOption.CENON_HD?65:55);//modify by csc form 45 to 55
              oa.setDuration(400);
		mMinus.setVisibility(View.VISIBLE);
	       ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x",  FeatureOption.CENON_HD?-65:-55,0);//modify by csc form 45 to 55
              oa2.setDuration(400);
		final AnimatorSet sets = new AnimatorSet();
              sets.setInterpolator(new AccelerateInterpolator());
              sets.playTogether(oa,oa2);
              sets.start();
              ConversationListAdapter.flag=true;
    }
   /**begin:add by csc for debug 2932**/
   public void changeStateToEdit1(){
	   mUnreadIcon.setAlpha(0f);//added by lzp for bug [2605]
   Log.v("sjb","changeStateToEdit------");
		ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x",  FeatureOption.CENON_HD?65:55);//modify by csc form 45 to 55
              oa.setDuration(0);
		mMinus.setVisibility(View.VISIBLE);
	       ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x", FeatureOption.CENON_HD?-65:-55,0);//modify by csc form 45 to 55
              oa2.setDuration(0);
		final AnimatorSet sets = new AnimatorSet();
              sets.setInterpolator(new AccelerateInterpolator());
              sets.playTogether(oa,oa2);
              sets.start();
              ConversationListAdapter.flag=true;
    }
   /**end:add by csc for debug 2932**/
      public void changeStateToComplete(){	
	  	Log.v(TAG,"changeStateToComplete---");
	  	mUnreadIcon.setAlpha(1f);//added by lzp for bug [2605]
		int tag = (Integer)mMinus.getTag();
		if(tag==BUTTON_SMSLEFT_ROTATE_TRUE){
			mMinus.setTag(BUTTON_SMSLEFT_ROTATE_FALSE);
		     mImgarror.setVisibility(View.VISIBLE);
		     ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x",  FeatureOption.CENON_HD?65:55);//modify by csc form 45 to 55
                   oa.setDuration(500);
		      oa.addListener(new AnimatorListener() {
					
					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
                                         mDelete.setVisibility(View.INVISIBLE);
		                           ObjectAnimator oaa=ObjectAnimator.ofFloat(mSmsContentRow, "x", 0);
                                         oaa.setDuration(400);
	                                  ObjectAnimator oa22=ObjectAnimator.ofFloat(mMinus, "x",  FeatureOption.CENON_HD?-65:-55);//modify by csc form 45 to 55
                                         oa22.setDuration(400);
		                           final AnimatorSet sets = new AnimatorSet();
                                         sets.setInterpolator(new AccelerateInterpolator());
                                         sets.playTogether(oaa,oa22);
                                         sets.start();						

					}
					
					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub
						
					}
				});
		     mMinus.setVisibility(View.VISIBLE);
	            ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x", 0);
                   oa2.setDuration(500);
		     final AnimatorSet sets = new AnimatorSet();
                   sets.setInterpolator(new AccelerateInterpolator());
                   sets.playTogether(oa,oa2);
                   sets.start();	         
		}else{
		    ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x", 0);
                  oa.setDuration(400);
		    mMinus.setVisibility(View.VISIBLE);
	           ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x",  FeatureOption.CENON_HD?-65:-55);//modify by csc form 45 to 55
                  oa2.setDuration(400);
		    final AnimatorSet sets = new AnimatorSet();
                  sets.setInterpolator(new AccelerateInterpolator());
                  sets.playTogether(oa,oa2);
                  sets.start();
		}
		ConversationListAdapter.flag=false;
	}
      /**begin:add by csc for debug 2932**/
      public void changeStateToComplete1(){	
  	  	Log.v(TAG,"changeStateToComplete---");
  	  	mUnreadIcon.setAlpha(1f);//added by lzp for bug [2605]
  		int tag = (Integer)mMinus.getTag();
  		if(tag==BUTTON_SMSLEFT_ROTATE_TRUE){
  			mMinus.setTag(BUTTON_SMSLEFT_ROTATE_FALSE);
  		     mImgarror.setVisibility(View.VISIBLE);
  		     ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x",  FeatureOption.CENON_HD?65:55);//modify by csc form 45 to 55
                     oa.setDuration(500);
  		      oa.addListener(new AnimatorListener() {
  					
  					@Override
  					public void onAnimationStart(Animator animation) {
  						// TODO Auto-generated method stub
  						
  					}
  					
  					@Override
  					public void onAnimationRepeat(Animator animation) {
  						// TODO Auto-generated method stub
  						
  					}
  					
  					@Override
  					public void onAnimationEnd(Animator animation) {
  						// TODO Auto-generated method stub
                                           mDelete.setVisibility(View.INVISIBLE);
  		                           ObjectAnimator oaa=ObjectAnimator.ofFloat(mSmsContentRow, "x", 0);
                                           oaa.setDuration(400);
  	                                  ObjectAnimator oa22=ObjectAnimator.ofFloat(mMinus, "x",  FeatureOption.CENON_HD?-65:-55);//modify by csc form 45 to 55
                                           oa22.setDuration(400);
  		                           final AnimatorSet sets = new AnimatorSet();
                                           sets.setInterpolator(new AccelerateInterpolator());
                                           sets.playTogether(oaa,oa22);
                                           sets.start();						

  					}
  					
  					@Override
  					public void onAnimationCancel(Animator animation) {
  						// TODO Auto-generated method stub
  						
  					}
  				});
  		     mMinus.setVisibility(View.VISIBLE);
  	            ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x", 0);
                     oa2.setDuration(500);
  		     final AnimatorSet sets = new AnimatorSet();
                     sets.setInterpolator(new AccelerateInterpolator());
                     sets.playTogether(oa,oa2);
                     sets.start();	         
  		}else{
  		    ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x", 0);
                    oa.setDuration(0);
  		    mMinus.setVisibility(View.VISIBLE);
  	           ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x",  FeatureOption.CENON_HD?-65:-55);//modify by csc form 45 to 55
                    oa2.setDuration(0);
  		    final AnimatorSet sets = new AnimatorSet();
                    sets.setInterpolator(new AccelerateInterpolator());
                    sets.playTogether(oa,oa2);
                    sets.start();
  		}
  		ConversationListAdapter.flag=false;
  	}
      /**end:add by csc for debug 2932**/
	 public void slideToDelete(){
            mDelete.clearAnimation();
	     deleteButtonDisplay(true);
	 }
	  public void cancelDelete(){
             mDelete.clearAnimation();
	      deleteButtonDisplay(false);
	  }


	private TranslateAnimation translateAnimTextToEdit = null, translateAnimMinusToEdit = null,
		translateAnimTextToComplete = null, translateAnimMinusToComplete = null;
	private Animation getTextAnimToEdit() {
		if(translateAnimTextToEdit == null) {
			translateAnimTextToEdit = new TranslateAnimation(0,mMinus.getMeasuredWidth(),0,0);
			if(((RelativeLayout.LayoutParams)mSmsContentRow.getLayoutParams()).leftMargin == mMinus.getWidth()){
				translateAnimTextToEdit = new TranslateAnimation(-mMinus.getMeasuredWidth(),0,0,0);
			}
			translateAnimTextToEdit.setDuration(DURATION);
			translateAnimTextToEdit.setFillAfter(true);
		}
		return translateAnimTextToEdit;
	}
	private Animation getMinusAnimToEdit() {
		if(translateAnimMinusToEdit == null) {
			translateAnimMinusToEdit = new TranslateAnimation(-mMinus.getWidth(),0,0,0); 
			translateAnimMinusToEdit.setDuration(DURATION);
		}
		return translateAnimMinusToEdit;
	}
	private Animation getTextAnimToComplete() {
		if(translateAnimTextToComplete == null) {
			translateAnimTextToComplete = new TranslateAnimation(mMinus.getMeasuredWidth(),0,0,0);
			if(((RelativeLayout.LayoutParams)mSmsContentRow.getLayoutParams()).leftMargin == mMinus.getWidth()){
				translateAnimTextToComplete = new TranslateAnimation(0,-mMinus.getMeasuredWidth(),0,0);
			}
			translateAnimTextToComplete.setDuration(DURATION);
			translateAnimTextToComplete.setFillAfter(true);
		}
		return translateAnimTextToComplete;
	}
	private Animation getMinusAnimToComplete() {
		if(translateAnimMinusToComplete == null) {
			translateAnimMinusToComplete = new TranslateAnimation(0,-mMinus.getWidth(),0,0); 
			translateAnimMinusToComplete.setDuration(DURATION);
		}
		return translateAnimMinusToComplete;
	}
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int tag = (Integer)v.getTag();
		switch (tag) {
		case BUTTON_SMSLEFT_ROTATE_TRUE:
		case BUTTON_SMSLEFT_ROTATE_FALSE://圆转动的时候
			/**Begin: modified by lzp  for bug [2575]**/
			    if(mDeleteItemOfSmsListListener.getDeleteState()){
					mDeleteItemOfSmsListListener.fakeTouchListView();
					return;
				}
				final boolean soRotate = (Integer)mMinus.getTag() == BUTTON_SMSLEFT_ROTATE_FALSE;
				toRotate(soRotate);//转动
				mDeleteItemOfSmsListListener.changeDeleteState(soRotate);
			/**End: modified by lzp **/ 
			break;
		case BUTTON_DELETE:
			//mDelete.clearAnimation();
		       changeStateToEdit();
                     long mThreadId = mConversation.getThreadId();
			if(mDeleteItemOfSmsListListener!=null){
                         mDeleteItemOfSmsListListener.onDeleteButtonClick(mThreadId);
			    mDeleteItemOfSmsListListener.updateStateOfEditButton();
			    mDeleteItemOfSmsListListener.smsItemSlideOut(mThreadId);
			}
			
			break;
		}

	}

       public void toRotate(boolean state) {
	   	Log.v(TAG,"toRotate---");
		if (state) {//转动		
			mMinus.setTag(BUTTON_SMSLEFT_ROTATE_TRUE);
			deleteButtonDisplay(true);//显示删除按钮
		} else {//还原
		       int tag = (Integer)mMinus.getTag();
			if(tag == BUTTON_SMSLEFT_ROTATE_FALSE)
				return;
			mMinus.setTag(BUTTON_SMSLEFT_ROTATE_FALSE);
			deleteButtonDisplay(false);	 
		}
	}
    /**Begin: added by lzp for bug [2575] **/
    public void setBackgroundEnable(boolean enable){
	    mSmsContentRow.setBackgroundResource(enable?R.drawable.listview_bg:0);
	}
    /**End: added by lzp for bug [2575] **/
	/**
	 * 显示或者隐cang删除按钮
	 * @param display
	 */
	public void deleteButtonDisplay(boolean display) {
	       Log.v(TAG,"deleteButtonDisplay---");
		if (display) {
			setBackgroundEnable(true);//added by lzp for bug [2575]
		     mDelete.setVisibility(View.VISIBLE);
		     mImgarror.setVisibility(View.GONE);
		     ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x", -mDelete.getWidth());
                   oa.setDuration(500);
		     mMinus.setVisibility(View.VISIBLE);
	            ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x", -mDelete.getWidth());
                   oa2.setDuration(500);
		     final AnimatorSet sets = new AnimatorSet();
                   sets.setInterpolator(new AccelerateInterpolator());
                   sets.playTogether(oa,oa2);
                   sets.start();			
		} else {
		     mImgarror.setVisibility(View.VISIBLE);
		     setBackgroundEnable(true);//added by lzp for bug [2575]
		     ObjectAnimator oa=ObjectAnimator.ofFloat(mSmsContentRow, "x",  FeatureOption.CENON_HD?65:55);//modify by csc form 45 to 55
                   oa.setDuration(500);
		      oa.addListener(new AnimatorListener() {
					
					@Override
					public void onAnimationStart(Animator animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animator animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
                                         mDelete.setVisibility(View.INVISIBLE);
                                         setBackgroundEnable(false);//added by lzp for bug [2575]
					}
					
					@Override
					public void onAnimationCancel(Animator animation) {
						// TODO Auto-generated method stub
						
					}
				});
		     mMinus.setVisibility(View.VISIBLE);
	            ObjectAnimator oa2=ObjectAnimator.ofFloat(mMinus, "x", 0);
                   oa2.setDuration(500);
		     final AnimatorSet sets = new AnimatorSet();
                   sets.setInterpolator(new AccelerateInterpolator());
                   sets.playTogether(oa,oa2);
                   sets.start();			
		}
	} 
	public interface DeleteItemOfSmsListListener{
           public void onDeleteButtonClick(Long mThreadId);
	    public void updateStateOfEditButton();
	    public void smsItemSlideOut(Long mThreadId);
	    public void changeDeleteState(boolean isDeleteState);//added by lzp for bug [2575] 
	    public boolean getDeleteState();
	    public void fakeTouchListView();
	}

	public void setDeleteItemOfSmsListListener(DeleteItemOfSmsListListener mListener){
           mDeleteItemOfSmsListListener = mListener;
	}
}
