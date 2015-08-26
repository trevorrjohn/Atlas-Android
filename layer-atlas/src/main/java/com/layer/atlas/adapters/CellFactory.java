package com.layer.atlas.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;

public interface CellFactory {
    boolean isBindable(Message message);

    CellHolder onCreateCellHolder(ViewGroup cellView, LayoutInflater layoutInflater);

    void onBindCellHolder(CellHolder cellHolder, boolean isMe, int position);
}
