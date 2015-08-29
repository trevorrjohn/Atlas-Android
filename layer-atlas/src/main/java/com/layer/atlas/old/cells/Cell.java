package com.layer.atlas.old.cells;

import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.MessagePart;

public abstract class Cell {
    public final MessagePart messagePart;
    public int clusterHeadItemId;
    public int clusterItemId;
    public boolean clusterTail;
    public boolean timeHeader;

    /**
     * if true, than previous message was from different user
     */
    public boolean firstUserMsg;
    /**
     * if true, than next message is from different user
     */
    public boolean lastUserMsg;

    /**
     * don't move left and right
     */
    public boolean noDecorations;

    public Cell(MessagePart messagePart) {
        this.messagePart = messagePart;
    }

    public Cell(MessagePart messagePart, boolean noDecorations) {
        this.messagePart = messagePart;
        this.noDecorations = noDecorations;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ ")
                .append("messagePart: ").append(messagePart.getMimeType())
                .append(": ").append(messagePart.getSize() < 2048 ? new String(messagePart.getData()) : messagePart.getSize() + " bytes")
                .append(", clusterId: ").append(clusterHeadItemId)
                .append(", clusterItem: ").append(clusterItemId)
                .append(", clusterTail: ").append(clusterTail)
                .append(", timeHeader: ").append(timeHeader).append(" ]");
        return builder.toString();
    }

    public void reset() {
        clusterHeadItemId = 0;
        clusterItemId = 0;
        clusterTail = false;
        timeHeader = false;
        firstUserMsg = false;
        lastUserMsg = false;
    }

    /**
     * Start with inflating your own cell.xml
     * <pre>
     * View rootView = Tools.findChildById(cellContainer, R.id.atlas_view_messages_cell_image);
     * if (rootView == null) {
     * rootView = LayoutInflater.from(cellContainer.getContext()).inflate(R.layout.atlas_view_messages_cell_image, cellContainer, false);
     * }
     * // ...
     * return rootView;
     * </pre>
     */
    public abstract View onBind(ViewGroup cellContainer);
}
