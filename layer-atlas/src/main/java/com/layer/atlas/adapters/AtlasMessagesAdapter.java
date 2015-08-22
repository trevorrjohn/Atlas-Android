package com.layer.atlas.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.atlas.ParticipantProvider;
import com.layer.atlas.R;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Wires a RecyclerViewController to a RecyclerView.  Extend this class with custom
 * RecyclerView.Adapters.
 */
public abstract class AtlasMessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> implements RecyclerViewController.Callback {
    protected final LayerClient mLayerClient;
    protected final ParticipantProvider mParticipantProvider;
    private final RecyclerViewController<Message> mQueryController;
    protected final LayoutInflater mLayoutInflater;
    protected final Handler mUiThreadHandler;
    protected OnAppendListener mAppendListener;

    protected int mViewTypeCount = 0;
    protected final Map<Integer, CellType> mCellTypesByViewType = new HashMap<Integer, CellType>();
    protected final Map<CellType, Integer> mViewTypesByCellType = new HashMap<CellType, Integer>();


    public AtlasMessagesAdapter(Context context, LayerClient client, ParticipantProvider participantProvider) {
        mLayerClient = client;
        mParticipantProvider = participantProvider;
        mQueryController = client.newRecyclerViewController(null, null, this);
        mLayoutInflater = LayoutInflater.from(context);
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        setHasStableIds(false);
    }

    /**
     * Merge with cellView and cache merged views in returned Object.
     *
     * @param cellView
     * @param layoutInflater
     * @param cellType
     * @return
     */
    public abstract Object onCreateCellHolder(ViewGroup cellView, LayoutInflater layoutInflater, int cellType);

    public abstract void onBindCellHolder(Object cellHolder, int cellType, Message message, int position);

    /**
     * Returns the cell type (previously registered with registerCellTypes) for this Message.
     *
     * @param message
     * @return
     */
    public abstract int getCellType(Message message);

    public AtlasMessagesAdapter registerCellTypes(int... cellTypes) {
        for (int cellType : cellTypes) {
            mViewTypeCount++;
            CellType me = new CellType(true, cellType);
            mCellTypesByViewType.put(mViewTypeCount, me);
            mViewTypesByCellType.put(me, mViewTypeCount);

            mViewTypeCount++;
            CellType notMe = new CellType(false, cellType);
            mCellTypesByViewType.put(mViewTypeCount, notMe);
            mViewTypesByCellType.put(notMe, mViewTypeCount);
        }
        return this;
    }

    public AtlasMessagesAdapter setQuery(Query<Message> query) {
        mQueryController.setQuery(query);
        return this;
    }

    /**
     * Refreshes this adapter by re-running the underlying Query.
     */
    public void refresh() {
        mQueryController.execute();
    }

    public Integer getPosition(Message queryable) {
        return mQueryController.getPosition(queryable);
    }

    public Integer getPosition(Message queryable, int lastPosition) {
        return mQueryController.getPosition(queryable, lastPosition);
    }

    public Message getItem(int position) {
        return mQueryController.getItem(position);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CellType cellType = mCellTypesByViewType.get(viewType);
        int rootResId = cellType.mMe ? R.layout.atlas_message_item_me : R.layout.atlas_message_item_them;
        MessageViewHolder rootViewHolder = new MessageViewHolder(mLayoutInflater.inflate(rootResId, null));
        rootViewHolder.mCellHolder = onCreateCellHolder(rootViewHolder.mCellView, mLayoutInflater, cellType.mType);
        return rootViewHolder;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
        int cellType = mCellTypesByViewType.get(viewHolder.getItemViewType()).mType;
        onBindCellHolder(viewHolder.mCellHolder, cellType, getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        boolean isMe = mLayerClient.getAuthenticatedUserId().equals(message.getSender().getUserId());
        int cellType = getCellType(message);
        return mViewTypesByCellType.get(new CellType(isMe, cellType));
    }

    @Override
    public int getItemCount() {
        return mQueryController.getItemCount();
    }

    /**
     * Sets the OnAppendListener for this AtlasQueryAdapter.  The listener will be called when items
     * are appended to the end of this adapter.  This is useful for implementing a scroll-to-bottom
     * feature.
     *
     * @param listener The OnAppendListener to notify about appended items.
     * @return This AtlasQueryAdapter.
     */
    public AtlasMessagesAdapter setOnAppendListener(OnAppendListener listener) {
        mAppendListener = listener;
        return this;
    }


    //==============================================================================================
    // TODO
    //==============================================================================================
//    private final DateFormat mDateFormat;
//    private final DateFormat mTimeFormat;

//    mDateFormat = android.text.format.DateFormat.getDateFormat(context);
//    mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);


//    @Override
//    public void onBindViewHolder(ViewHolder v, Message message, int messagePosition) {
//        LayerClient client = App.getLayerClient();
//
//        v.mMessage = message;
//
//        // Turn stuff off
//        v.mSpacerRight.setVisibility(View.GONE);
//        v.mReceiptView.setVisibility(View.GONE);
//
//        // Name
//        Actor sender = message.getSender();
//        if (sender.getName() != null) {
//            v.mUserNameHeader.setText(sender.getName());
//        } else {
//            Participant participant = mProvider.getParticipant(sender.getUserId());
//            v.mUserNameHeader.setText(participant != null ? participant.getName() : "...");
//        }
//
//        // Clustering
//        Cluster cluster = getClustering(message, messagePosition);
//        if (cluster.mDateBoundaryWithPrevious) {
//            Date sentAt = message.getSentAt();
//            if (sentAt == null) sentAt = new Date();
//            String timeBarDayText = Utils.formatTimeDay(sentAt);
//            v.mTimeBarDay.setText(timeBarDayText);
//            String timeBarTimeText = mTimeFormat.format(sentAt.getTime());
//            v.mTimeBarTime.setText(timeBarTimeText);
//
//            v.mTimeBar.setVisibility(View.VISIBLE);
//            v.mSpacerTop1.setVisibility(View.GONE);
//            v.mSpacerTop2.setVisibility(View.GONE);
//        } else {
//            v.mTimeBar.setVisibility(View.GONE);
//            if (cluster.mClusterWithPrevious != null) {
//                switch (cluster.mClusterWithPrevious) {
//                    case MINUTE:
//                        v.mSpacerTop1.setVisibility(View.GONE);
//                        v.mSpacerTop2.setVisibility(View.GONE);
//                        break;
//                    case HOUR:
//                        v.mSpacerTop1.setVisibility(View.VISIBLE);
//                        v.mSpacerTop2.setVisibility(View.GONE);
//                        break;
//                    case MORE_THAN_HOUR:
//                        v.mSpacerTop1.setVisibility(View.VISIBLE);
//                        v.mSpacerTop2.setVisibility(View.VISIBLE);
//                        break;
//                    case NEW_SENDER:
//                        v.mSpacerTop1.setVisibility(View.VISIBLE);
//                        v.mSpacerTop2.setVisibility(View.VISIBLE);
//                        break;
//                }
//            } else {
//                // No previous message
//                v.mSpacerTop1.setVisibility(View.GONE);
//                v.mSpacerTop2.setVisibility(View.GONE);
//            }
//        }
//
//        // Cell content
//        mCellBinder.onBindMessageCell(client, message, v.mCellContainer);
//    }

    private final Map<Uri, Cluster> mClusterCache = new HashMap<Uri, Cluster>();

    private Cluster getClustering(Message message, int position) {
        Cluster result = mClusterCache.get(message.getId());
        if (result == null) {
            result = new Cluster();
            mClusterCache.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.mDateBoundaryWithPrevious = isDateBoundary(previousMessage.getSentAt(), message.getSentAt());
            result.mClusterWithPrevious = ClusterType.fromMessages(previousMessage, message);

            Cluster previousCluster = mClusterCache.get(previousMessage.getId());
            if (previousCluster == null) {
                previousCluster = new Cluster();
                mClusterCache.put(previousMessage.getId(), previousCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousCluster.mClusterWithNext != result.mClusterWithPrevious) ||
                        (previousCluster.mDateBoundaryWithNext != result.mDateBoundaryWithPrevious)) {
                    requestUpdate(previousMessage, previousPosition);
                }
            }
            previousCluster.mClusterWithNext = result.mClusterWithPrevious;
            previousCluster.mDateBoundaryWithNext = result.mDateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.mDateBoundaryWithNext = isDateBoundary(message.getSentAt(), nextMessage.getSentAt());
            result.mClusterWithNext = ClusterType.fromMessages(message, nextMessage);

            Cluster nextCluster = mClusterCache.get(nextMessage.getId());
            if (nextCluster == null) {
                nextCluster = new Cluster();
                mClusterCache.put(nextMessage.getId(), nextCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextCluster.mClusterWithPrevious != result.mClusterWithNext) ||
                        (nextCluster.mDateBoundaryWithPrevious != result.mDateBoundaryWithNext)) {
                    requestUpdate(nextMessage, nextPosition);
                }
            }
            nextCluster.mClusterWithPrevious = result.mClusterWithNext;
            nextCluster.mDateBoundaryWithPrevious = result.mDateBoundaryWithNext;
        }

        return result;
    }

    private void requestUpdate(final Message message, final int lastPosition) {
        mUiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(getPosition(message, lastPosition));
            }
        });
    }

    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay() != d2.getDay());
    }

    private enum ClusterType {
        NEW_SENDER,
        MINUTE,
        HOUR,
        MORE_THAN_HOUR;

        private static final long MILLIS_MINUTE = 60 * 1000;
        private static final long MILLIS_HOUR = 60 * MILLIS_MINUTE;

        public static ClusterType fromMessages(Message older, Message newer) {
            // Different users?
            if (!older.getSender().equals(newer.getSender())) return NEW_SENDER;

            // Time clustering for same user?
            Date oldSentAt = older.getSentAt();
            Date newSentAt = newer.getSentAt();
            if (oldSentAt == null || newSentAt == null) return MINUTE;
            long delta = Math.abs(newSentAt.getTime() - oldSentAt.getTime());
            if (delta <= MILLIS_MINUTE) return MINUTE;
            if (delta <= MILLIS_HOUR) return HOUR;
            return MORE_THAN_HOUR;
        }
    }

    private static class Cluster {
        public boolean mDateBoundaryWithPrevious;
        public ClusterType mClusterWithPrevious;

        public boolean mDateBoundaryWithNext;
        public ClusterType mClusterWithNext;
    }


    //==============================================================================================
    // UI update callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        notifyDataSetChanged();
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        notifyItemChanged(position);
    }

    @Override
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        notifyItemInserted(position);
        if (mAppendListener != null && (position + 1) == getItemCount()) {
            mAppendListener.onItemAppended(this, getItem(position));
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
        int positionEnd = positionStart + itemCount;
        if (mAppendListener != null && (positionEnd + 1) == getItemCount()) {
            mAppendListener.onItemAppended(this, getItem(positionEnd));
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        notifyItemRemoved(position);
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    protected static class CellType {
        protected final boolean mMe;
        protected final int mType;

        public CellType(boolean isMe, int type) {
            mMe = isMe;
            mType = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CellType cellType = (CellType) o;

            if (mMe != cellType.mMe) return false;
            return mType == cellType.mType;

        }

        @Override
        public int hashCode() {
            int result = (mMe ? 1 : 0);
            result = 31 * result + mType;
            return result;
        }
    }

    /**
     * Listens inserts to the end of an AtlasQueryAdapter.
     */
    public interface OnAppendListener {
        /**
         * Alerts the listener to inserts at the end of an AtlasQueryAdapter.  If a batch of items
         * were appended, only the last one will be alerted here.
         *
         * @param adapter The AtlasQueryAdapter which had an item appended.
         * @param message The item appended to the AtlasQueryAdapter.
         */
        void onItemAppended(AtlasMessagesAdapter adapter, Message message);
    }
}