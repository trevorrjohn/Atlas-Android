package com.layer.atlas.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;

public interface MessageCell {
    boolean isCellType(Message message);

    Object onCreateCellHolder(ViewGroup cellView, LayoutInflater layoutInflater);

    void onBindCellHolder(Object cellHolder, boolean isMe, Message message, int position);
}
