package com.layer.atlas;

import android.graphics.drawable.Drawable;

import java.util.Comparator;

/**
 * Participant allows Atlas classes to display information about users, like Message senders,
 * Conversation participants, TypingIndicator users, etc.
 */
public interface Participant {
    /**
     * Returns the name of this Participant.
     *
     * @return The name of this Participant
     */
    String getName();

    /**
     * Returns drawable to be used as paprticipant's avatar in Atlas Views.
     * If undefined, initials would be used instead.
     *
     * @return drawable, or null
     */
    Drawable getAvatarDrawable();

    Comparator<Participant> COMPARATOR = new Utils.FilteringComparator("");
}
