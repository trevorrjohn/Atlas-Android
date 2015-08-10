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
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.layer.sdk.messaging.Message;

/**
 * @author Oleg Orlov
 * @since 13 May 2015
 */
public class AtlasMessageList extends RecyclerView {
    private static final String TAG = AtlasMessageList.class.getSimpleName();

    //styles
    public int mMyBubbleColor;
    public int mMyTextColor;
    public int mMyTextStyle;
    private float mMyTextSize;
    public Typeface mMyTextTypeface;

    public int mOtherBubbleColor;
    public int mOtherTextColor;
    public int mOtherTextStyle;
    private float mOtherTextSize;
    public Typeface mOtherTextTypeface;

    private int mDateTextColor;
    private int mAvatarTextColor;
    private int mAvatarBackgroundColor;

    public AtlasMessageList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs, defStyle);
    }

    public AtlasMessageList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AtlasMessageList(Context context) {
        super(context);
    }

    public AtlasMessageList init() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setStackFromEnd(true);
        setLayoutManager(manager);
        return this;
    }

    public AtlasMessageList setAdapter(AtlasQueryAdapter<Message, ? extends ViewHolder> adapter) {
        super.setAdapter(adapter);
        return this;
    }

    public AtlasMessageList refresh() {
        AtlasQueryAdapter adapter = (AtlasQueryAdapter) getAdapter();
        if (adapter != null) adapter.refresh();
        return this;
    }

    public void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AtlasMessageList, R.attr.AtlasMessageList, defStyle);
        mMyTextColor = ta.getColor(R.styleable.AtlasMessageList_myTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mMyTextStyle = ta.getInt(R.styleable.AtlasMessageList_myTextStyle, Typeface.NORMAL);
        String myTextTypefaceName = ta.getString(R.styleable.AtlasMessageList_myTextTypeface);
        mMyTextTypeface = myTextTypefaceName != null ? Typeface.create(myTextTypefaceName, mMyTextStyle) : null;
        //mMyTextSize = ta.getDimension(R.styleable.AtlasMessageList_myTextSize, context.getResources().getDimension(R.dimen.atlas_text_size_general));

        mOtherTextColor = ta.getColor(R.styleable.AtlasMessageList_theirTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mOtherTextStyle = ta.getInt(R.styleable.AtlasMessageList_theirTextStyle, Typeface.NORMAL);
        String otherTextTypefaceName = ta.getString(R.styleable.AtlasMessageList_theirTextTypeface);
        mOtherTextTypeface = otherTextTypefaceName != null ? Typeface.create(otherTextTypefaceName, mOtherTextStyle) : null;
        //mOtherTextSize = ta.getDimension(R.styleable.AtlasMessageList_theirTextSize, context.getResources().getDimension(R.dimen.atlas_text_size_general));

        mMyBubbleColor = ta.getColor(R.styleable.AtlasMessageList_myBubbleColor, context.getResources().getColor(R.color.atlas_bubble_blue));
        mOtherBubbleColor = ta.getColor(R.styleable.AtlasMessageList_theirBubbleColor, context.getResources().getColor(R.color.atlas_background_gray));

        mDateTextColor = ta.getColor(R.styleable.AtlasMessageList_dateTextColor, context.getResources().getColor(R.color.atlas_text_gray));
        mAvatarTextColor = ta.getColor(R.styleable.AtlasMessageList_avatarTextColor, context.getResources().getColor(R.color.atlas_text_black));
        mAvatarBackgroundColor = ta.getColor(R.styleable.AtlasMessageList_avatarBackgroundColor, context.getResources().getColor(R.color.atlas_background_gray));
        ta.recycle();
    }
}
