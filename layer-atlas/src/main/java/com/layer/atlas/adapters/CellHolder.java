package com.layer.atlas.adapters;

import android.view.View;

import com.layer.sdk.messaging.Message;

public abstract class CellHolder implements View.OnClickListener, View.OnLongClickListener {
    private OnClickListener mClickListener;
    private Message mMessage;

    public CellHolder setClickableView(View clickableView) {
        clickableView.setOnClickListener(this);
        clickableView.setOnLongClickListener(this);
        return this;
    }

    public CellHolder setMessage(Message message) {
        mMessage = message;
        return this;
    }

    public Message getMessage() {
        return mMessage;
    }

    public CellHolder setClickListener(OnClickListener clickListener) {
        mClickListener = clickListener;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener == null) return;
        mClickListener.onClick(this);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mClickListener == null) return false;
        return mClickListener.onLongClick(this);
    }

    public interface OnClickListener {
        void onClick(CellHolder cellHolder);

        boolean onLongClick(CellHolder cellHolder);
    }
}
