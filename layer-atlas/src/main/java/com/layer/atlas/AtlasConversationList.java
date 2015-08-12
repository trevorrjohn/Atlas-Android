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
package com.layer.atlas;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.Query;

/**
 * @author Oleg Orlov
 * @since 14 May 2015
 */
public class AtlasConversationList extends RecyclerView {
    private static final String TAG = AtlasConversationList.class.getSimpleName();

    //styles
    private int mTitleTextColor;
    private int mTitleTextStyle;
    private Typeface mTitleTextTypeface;
    private int mTitleUnreadTextColor;
    private int mTitleUnreadTextStyle;
    private Typeface mTitleUnreadTextTypeface;
    private int mSubtitleTextColor;
    private int mSubtitleTextStyle;
    private Typeface mSubtitleTextTypeface;
    private int mSubtitleUnreadTextColor;
    private int mSubtitleUnreadTextStyle;
    private Typeface mSubtitleUnreadTextTypeface;
    private int mCellBackgroundColor;
    private int mCellUnreadBackgroundColor;
    private int mDateTextColor;
    private int mAvatarTextColor;
    private int mAvatarBackgroundColor;

    public AtlasConversationList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs, defStyle);
    }

    public AtlasConversationList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AtlasConversationList(Context context) {
        super(context);
    }

    public AtlasConversationList init(AtlasQueryAdapter<Conversation, ? extends ViewHolder> adapter) {
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setStackFromEnd(false);
        setLayoutManager(manager);
        setAdapter(adapter);
        return this;
    }

    public AtlasConversationList setAdapter(AtlasQueryAdapter<Conversation, ? extends ViewHolder> adapter) {
        super.setAdapter(adapter);
        return this;
    }

    public AtlasConversationList refresh() {
        AtlasQueryAdapter adapter = (AtlasQueryAdapter) getAdapter();
        if (adapter != null) adapter.refresh();
        return this;
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AtlasConversationList, R.attr.AtlasConversationList, defStyle);
        mTitleTextColor = ta.getColor(R.styleable.AtlasConversationList_cellTitleTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mTitleTextStyle = ta.getInt(R.styleable.AtlasConversationList_cellTitleTextStyle, Typeface.NORMAL);
        String titleTextTypefaceName = ta.getString(R.styleable.AtlasConversationList_cellTitleTextTypeface);
        mTitleTextTypeface = titleTextTypefaceName != null ? Typeface.create(titleTextTypefaceName, mTitleTextStyle) : null;

        mTitleUnreadTextColor = ta.getColor(R.styleable.AtlasConversationList_cellTitleUnreadTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mTitleUnreadTextStyle = ta.getInt(R.styleable.AtlasConversationList_cellTitleUnreadTextStyle, Typeface.BOLD);
        String titleUnreadTextTypefaceName = ta.getString(R.styleable.AtlasConversationList_cellTitleUnreadTextTypeface);
        mTitleUnreadTextTypeface = titleUnreadTextTypefaceName != null ? Typeface.create(titleUnreadTextTypefaceName, mTitleUnreadTextStyle) : null;

        mSubtitleTextColor = ta.getColor(R.styleable.AtlasConversationList_cellSubtitleTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mSubtitleTextStyle = ta.getInt(R.styleable.AtlasConversationList_cellSubtitleTextStyle, Typeface.NORMAL);
        String subtitleTextTypefaceName = ta.getString(R.styleable.AtlasConversationList_cellSubtitleTextTypeface);
        mSubtitleTextTypeface = subtitleTextTypefaceName != null ? Typeface.create(subtitleTextTypefaceName, mSubtitleTextStyle) : null;

        mSubtitleUnreadTextColor = ta.getColor(R.styleable.AtlasConversationList_cellSubtitleUnreadTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mSubtitleUnreadTextStyle = ta.getInt(R.styleable.AtlasConversationList_cellSubtitleUnreadTextStyle, Typeface.NORMAL);
        String subtitleUnreadTextTypefaceName = ta.getString(R.styleable.AtlasConversationList_cellSubtitleUnreadTextTypeface);
        mSubtitleUnreadTextTypeface = subtitleUnreadTextTypefaceName != null ? Typeface.create(subtitleUnreadTextTypefaceName, mSubtitleUnreadTextStyle) : null;

        mCellBackgroundColor = ta.getColor(R.styleable.AtlasConversationList_cellBackgroundColor, Color.TRANSPARENT);
        mCellUnreadBackgroundColor = ta.getColor(R.styleable.AtlasConversationList_cellUnreadBackgroundColor, Color.TRANSPARENT);
        mDateTextColor = ta.getColor(R.styleable.AtlasConversationList_dateTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mAvatarTextColor = ta.getColor(R.styleable.AtlasConversationList_avatarTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mAvatarBackgroundColor = ta.getColor(R.styleable.AtlasConversationList_avatarBackgroundColor, context.getResources().getColor(R.color.atlas_shape_avatar_gray));
        ta.recycle();
    }

    /**
     * Convenience pass-through to this list's AtlasQueryAdapter.setOnItemClickListener().
     *
     * @see AtlasQueryAdapter#setOnItemClickListener(AtlasQueryAdapter.OnItemClickListener)
     */
    @SuppressWarnings("unchecked")
    public AtlasConversationList setOnItemClickListener(AtlasQueryAdapter.OnItemClickListener<Conversation> listener) {
        ((AtlasQueryAdapter<Conversation, ? extends ViewHolder>) getAdapter()).setOnItemClickListener(listener);
        return this;
    }

    /**
     * Convenience pass-through to this list's AtlasQueryAdapter.setOnAppendListener().
     *
     * @see AtlasQueryAdapter#setOnAppendListener(AtlasQueryAdapter.OnAppendListener)
     */
    @SuppressWarnings("unchecked")
    public AtlasConversationList setOnAppendListener(AtlasQueryAdapter.OnAppendListener<Conversation> listener) {
        ((AtlasQueryAdapter<Conversation, ? extends ViewHolder>) getAdapter()).setOnAppendListener(listener);
        return this;
    }

    /**
     * Convenience pass-through to this list's AtlasQueryAdapter.setQuery().
     *
     * @see AtlasQueryAdapter#setQuery(Query)
     */
    @SuppressWarnings("unchecked")
    public AtlasConversationList setQuery(Query<Conversation> query) {
        ((AtlasQueryAdapter<Conversation, ? extends ViewHolder>) getAdapter()).setQuery(query);
        return this;
    }
}
