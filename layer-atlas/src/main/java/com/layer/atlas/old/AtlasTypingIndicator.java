/*
 * Copyright (c) 2015 Layer. All rights reserved.
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
package com.layer.atlas.old;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.layer.atlas.Participant;
import com.layer.atlas.ParticipantProvider;
import com.layer.atlas.R;
import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AtlasTypingIndicator provides feedback about typists within a Conversation.  When initialized
 * and registered with a LayerClient as a LayerTypingIndicatorListener, AtlasTypingIndicator
 * maintains a set of typists for the given Conversation, providing callbacks when UI updates are
 * needed.  AtlasTypingIndicator can provide a default UI updater if desired.
 */
public class AtlasTypingIndicator extends FrameLayout implements LayerTypingIndicatorListener.Weak {

    private LayerClient mLayerClient;
    private volatile Conversation mConversation;
    private final Set<String> mTypists = new HashSet<String>();
    private TextView mTextView;
    private volatile Listener mListener;

    // styles
    private int mTextColor;
    private Typeface mTextTypeface;
    private int mTextStyle;
    private float mTextSize;

    public AtlasTypingIndicator(Context context) {
        super(context);
    }

    public AtlasTypingIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AtlasTypingIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs, defStyle);
    }

    /**
     * Initializes this AtlasTypingIndicator.
     *
     * @return This AtlasTypingIndicator for chaining.
     */
    public AtlasTypingIndicator init(LayerClient layerClient) {
        if (layerClient == null) throw new IllegalArgumentException("LayerClient cannot be null");
        if (mTextView != null) throw new IllegalStateException("AtlasTypingIndicator is already initialized!");
        mLayerClient = layerClient;
        mTextView = new TextView(getContext());
        addView(mTextView);
        applyStyle();
        mLayerClient.registerTypingIndicator(this);
        return this;
    }

    /**
     * Sets the Conversation to listen for typing on.  If `null`, no typing will be listened to.
     *
     * @param conversation Conversation to listen for typing on
     * @return This AtlasTypingIndicator for chaining.
     */
    public AtlasTypingIndicator setConversation(Conversation conversation) {
        mConversation = conversation;
        return this;
    }

    public AtlasTypingIndicator setListener(Listener listener) {
        if (listener == null) throw new IllegalArgumentException("Callback cannot be null");
        mListener = listener;
        return this;
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AtlasTypingIndicator, R.attr.AtlasTypingIndicator, defStyle);
        Resources r = context.getResources();
        mTextColor = ta.getColor(R.styleable.AtlasTypingIndicator_indicatorTextColor, r.getColor(R.color.atlas_text_black));
        mTextStyle = ta.getInt(R.styleable.AtlasTypingIndicator_indicatorTextStyle, Typeface.NORMAL);
        mTextTypeface = Typeface.create(ta.getString(R.styleable.AtlasTypingIndicator_indicatorTextTypeface), mTextStyle);
        mTextSize = ta.getDimension(R.styleable.AtlasTypingIndicator_indicatorTextSize, r.getDimension(R.dimen.atlas_text_size_general));
        ta.recycle();
    }

    private void applyStyle() {
        mTextView.setTextColor(mTextColor);
        mTextView.setTypeface(mTextTypeface);
        //mTextView.setTextSize(mTextSize);
    }

    /**
     * Clears the current list of typists and calls refresh().
     *
     * @return This AtlasTypingIndicator for chaining.
     */
    public AtlasTypingIndicator clear() {
        synchronized (mTypists) {
            mTypists.clear();
        }
        refresh();
        return this;
    }

    /**
     * Calls Callback.onTypingUpdate() with the current list of typists.
     *
     * @return This AtlasTypingIndicator for chaining.
     */
    private AtlasTypingIndicator refresh() {
        synchronized (mTypists) {
            if (mListener != null) mListener.onTypingUpdate(this, mTypists);
        }
        return this;
    }

    /**
     * Sets the text content of this AtlasTypingIndicator.
     *
     * @param text Text content
     */
    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    @Override
    public void onTypingIndicator(LayerClient layerClient, Conversation conversation, String userId, TypingIndicator typingIndicator) {
        if (mConversation != conversation) return;
        if (typingIndicator == TypingIndicator.FINISHED) {
            mTypists.remove(userId);
        } else {
            mTypists.add(userId);
        }
        refresh();
    }

    /**
     * AtlasTypingIndicator.Callback allows an external class to set indicator text, visibility,
     * etc. based on the current typists.
     */
    public interface Listener {
        /**
         * Notifies the callback to typist updates.
         *
         * @param indicator     The AtlasTypingIndicator notifying
         * @param typingUserIds The set of currently-active typist user IDs
         */
        void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds);
    }

    /**
     * Default Callback handler implementation.
     */
    public static class DefaultListener implements Listener {
        private final ParticipantProvider mParticipantProvider;

        public DefaultListener(ParticipantProvider participantProvider) {
            if (participantProvider == null) throw new IllegalArgumentException("ParticipantProvider cannot be null");
            mParticipantProvider = participantProvider;
        }

        @Override
        public void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds) {
            List<Participant> typists = new ArrayList<Participant>(typingUserIds.size());
            for (String userId : typingUserIds) {
                Participant participant = mParticipantProvider.getParticipant(userId);
                if (participant != null) typists.add(participant);
            }

            if (typists.isEmpty()) {
                indicator.setText(null);
                indicator.setVisibility(GONE);
                return;
            }

            String[] strings = indicator.getResources().getStringArray(R.array.atlas_typing_indicator);
            String string = strings[Math.min(strings.length - 1, typingUserIds.size())];
            String[] names = new String[typists.size()];
            int i = 0;
            for (Participant typist : typists) {
                names[i++] = typist.getName();
            }
            indicator.setText(String.format(string, names));
            indicator.setVisibility(VISIBLE);
        }
    }

}
