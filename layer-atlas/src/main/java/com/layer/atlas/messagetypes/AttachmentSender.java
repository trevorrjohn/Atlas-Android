package com.layer.atlas.messagetypes;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;

/**
 * AttachmentSenders populate the AtlasMessageComposer attachment menu and handle message sending
 * requests.  AttachmentSenders can interact with the Activity lifecycle to preserve instance state
 * and receive activity results when needed.
 */
public abstract class AttachmentSender extends MessageSender {
    private final String mTitle;
    private final Integer mIcon;

    public AttachmentSender(String title, Integer icon) {
        mTitle = title;
        mIcon = icon;
    }

    /**
     * Begins an attachment sending operation.  This might launch an Intent for selecting from a
     * gallery, taking a camera photo, or simply sending a message of the given type.  If an Intent
     * is generated for a result, consider overriding onActivityResult().
     *
     * @return `true` if a send operation is started, or `false` otherwise.
     * @see #onActivityResult(Activity, int, int, Intent)
     */
    public abstract boolean requestSend();

    /**
     * Override to save instance state.
     *
     * @return new saved instance state.
     * @see #onRestoreInstanceState(Parcelable)
     */
    public Parcelable onSaveInstanceState() {
        // Optional override
        return null;
    }

    /**
     * Override if saved instance state is required.
     *
     * @param state State previously created with onSaveInstanceState().
     * @see #onSaveInstanceState()
     */
    public void onRestoreInstanceState(Parcelable state) {
        // Optional override
    }

    /**
     * Override to handle results from onActivityResult.
     *
     * @return true if the result was handled, or false otherwise.
     */
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // Optional override
        return false;
    }

    /**
     * Override to handle results from onRequestPermissionsResult.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Optional override
    }

    /**
     * Returns the title for this AttachmentSender, typically for use in the AtlasMessageComposer
     * attachment menu.
     *
     * @return The title for this AttachmentSender.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the icon resource ID for this AttachmentSender, typically for use in the
     * AtlasMessageComposer attachment menu, or `null` for none.
     *
     * @return The icon resource ID for this AttachmentSender.
     */
    public Integer getIcon() {
        return mIcon;
    }
}
