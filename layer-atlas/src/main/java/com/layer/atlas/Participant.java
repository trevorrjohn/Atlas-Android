package com.layer.atlas;

import com.layer.atlas.old.Utils;

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

    String getAvatarUrl();

    Comparator<Participant> COMPARATOR = new Utils.FilteringComparator("");
}
