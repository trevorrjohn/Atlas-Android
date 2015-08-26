package com.layer.atlas.adapters;

import android.view.View;

import com.layer.sdk.messaging.Message;

/**
 * CellHolders maintain a reference to their Message, and allow the capture of user interactions
 * with their messages (e.g. clicks).  CellHolders can be extended to act as View caches, where
 * CellFactory.createCellHolder() might populate a CellHolder with references to Views for use in
 * future calls to CellFactory.bindCellHolder().
 */
public abstract class CellHolder implements View.OnClickListener, View.OnLongClickListener {
    private OnClickListener mClickListener;
    private Message mMessage;

    public CellHolder setClickableView(View clickableView) {
        clickableView.setOnClickListener(this);
        clickableView.setOnLongClickListener(this);
        return this;
    }

    protected CellHolder setMessage(Message message) {
        mMessage = message;
        return this;
    }

    public Message getMessage() {
        return mMessage;
    }

    protected CellHolder setClickListener(OnClickListener clickListener) {
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

    interface OnClickListener {
        void onClick(CellHolder cellHolder);

        boolean onLongClick(CellHolder cellHolder);
    }
}
