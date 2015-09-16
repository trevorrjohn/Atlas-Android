package com.layer.atlas.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.layer.atlas.AtlasCellFactory;
import com.layer.atlas.Participant;
import com.layer.atlas.ParticipantProvider;
import com.layer.atlas.R;
import com.layer.atlas.old.Utils;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Actor;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * AtlasMessagesAdapter drives an AtlasMessagesList.  The AtlasMessagesAdapter itself handles
 * rendering sender names, avatars, dates, left/right alignment, and message clustering, and leaves
 * rendering message content up to registered CellFactories.  Each CellFactory knows which Messages
 * it can render, can create new View hierarchies for its Message types, and can render (bind)
 * Message data with its created View hierarchies.  Typically, CellFactories are segregated by
 * MessagePart MIME types (e.g. "text/plain", "image/jpeg", and "application/vnd.geo+json").
 *
 * Under the hood, the AtlasMessagesAdapter is a RecyclerView.Adapter, which automatically recycles
 * its list items within view-type "buckets".  Each registered CellFactory actually creates two such
 * view-types: one for cells sent by the authenticated user, and another for cells sent by remote
 * actors.  This allows the AtlasMessagesAdapter to efficiently render images sent by the current
 * user aligned on the left, and images sent by others aligned on the right, for example.  In case
 * this sent-by distinction is of value when rendering cells, it provided as the `isMe` argument.
 *
 * When rendering Messages, the AtlasMessagesAdapter first determines which CellFactory to handle
 * the Message with calling CellFactory.isBindable() on each of its registered CellFactories. The
 * first CellFactory to return `true` is used for that Message.  Then, the adapter checks for
 * available CellHolders of that type.  If none are found, a new one is created with a call to
 * CellFactory.createCellHolder().  After creating a new CellHolder (or reusing an available one),
 * the CellHolder is rendered in the UI with Message data via CellFactory.bindCellHolder().
 *
 * @see AtlasCellFactory
 */
public class AtlasMessagesAdapter extends RecyclerView.Adapter<AtlasMessagesAdapter.ViewHolder> implements RecyclerViewController.Callback {
    protected final LayerClient mLayerClient;
    protected final ParticipantProvider mParticipantProvider;
    private final RecyclerViewController<Message> mQueryController;
    protected final LayoutInflater mLayoutInflater;
    protected final Handler mUiThreadHandler;
    protected OnMessageAppendListener mAppendListener;

    protected OnMessageClickListener mMessageClickListener;
    protected AtlasCellFactory.CellHolder.OnClickListener mCellHolderClickListener;

    // Cells
    protected int mViewTypeCount = 0;
    protected final Set<AtlasCellFactory> mCellFactories = new LinkedHashSet<AtlasCellFactory>();
    protected final Map<Integer, CellType> mCellTypesByViewType = new HashMap<Integer, CellType>();
    protected final Map<AtlasCellFactory, Integer> mMyViewTypesByCell = new HashMap<AtlasCellFactory, Integer>();
    protected final Map<AtlasCellFactory, Integer> mTheirViewTypesByCell = new HashMap<AtlasCellFactory, Integer>();

    // Dates and Clustering
    private final Map<Uri, Cluster> mClusterCache = new HashMap<Uri, Cluster>();
    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;

    // Read and delivery receipts
    private Map<Message.RecipientStatus, Message> mReceiptMap = new HashMap<Message.RecipientStatus, Message>();


    public AtlasMessagesAdapter(Context context, LayerClient client, ParticipantProvider participantProvider) {
        mLayerClient = client;
        mParticipantProvider = participantProvider;
        mQueryController = client.newRecyclerViewController(null, null, this);
        mLayoutInflater = LayoutInflater.from(context);
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        setHasStableIds(false);

        mCellHolderClickListener = new AtlasCellFactory.CellHolder.OnClickListener() {
            @Override
            public void onClick(AtlasCellFactory.CellHolder cellHolder) {
                if (mMessageClickListener == null) return;
                mMessageClickListener.onMessageClick(AtlasMessagesAdapter.this, cellHolder.getMessage());
            }

            @Override
            public boolean onLongClick(AtlasCellFactory.CellHolder cellHolder) {
                if (mMessageClickListener == null) return false;
                return mMessageClickListener.onMessageLongClick(AtlasMessagesAdapter.this, cellHolder.getMessage());
            }
        };
    }


    /**
     * Sets this AtlasMessagesAdapter's Message Query.
     *
     * @param query Query drive this AtlasMessagesAdapter.
     * @return This AtlasMessagesAdapter.
     */
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


    //==============================================================================================
    // Listeners
    //==============================================================================================

    /**
     * Sets the OnAppendListener for this AtlasQueryAdapter.  The listener will be called when items
     * are appended to the end of this adapter.  This is useful for implementing a scroll-to-bottom
     * feature.
     *
     * @param listener The OnAppendListener to notify about appended items.
     * @return This AtlasQueryAdapter.
     */
    public AtlasMessagesAdapter setOnMessageAppendListener(OnMessageAppendListener listener) {
        mAppendListener = listener;
        return this;
    }

    public AtlasMessagesAdapter setOnItemClickListener(OnMessageClickListener listener) {
        mMessageClickListener = listener;
        return this;
    }


    //==============================================================================================
    // Adapter and Cells
    //==============================================================================================

    /**
     * Registers one or more CellFactories for the AtlasMessagesAdapter to manage.  CellFactories
     * know which Messages they can render, and handle View caching, creation, and binding.
     *
     * @param cellFactories Cells to register.
     * @return This AtlasMessagesAdapter.
     */
    public AtlasMessagesAdapter registerCellFactories(AtlasCellFactory... cellFactories) {
        for (AtlasCellFactory CellFactory : cellFactories) {
            mCellFactories.add(CellFactory);

            mViewTypeCount++;
            CellType me = new CellType(true, CellFactory);
            mCellTypesByViewType.put(mViewTypeCount, me);
            mMyViewTypesByCell.put(CellFactory, mViewTypeCount);

            mViewTypeCount++;
            CellType notMe = new CellType(false, CellFactory);
            mCellTypesByViewType.put(mViewTypeCount, notMe);
            mTheirViewTypesByCell.put(CellFactory, mViewTypeCount);
        }
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CellType cellType = mCellTypesByViewType.get(viewType);
        int rootResId = cellType.mMe ? ViewHolder.RESOURCE_ID_ME : ViewHolder.RESOURCE_ID_THEM;
        ViewHolder rootViewHolder = new ViewHolder(mLayoutInflater.inflate(rootResId, parent, false));
        AtlasCellFactory.CellHolder cellHolder = cellType.mCellFactory.createCellHolder(rootViewHolder.mCellView, cellType.mMe, mLayoutInflater);
        cellHolder.setClickableView(rootViewHolder.itemView);
        cellHolder.setClickListener(mCellHolderClickListener);
        rootViewHolder.mCellHolder = cellHolder;
        return rootViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = getItem(position);
        CellType cellType = mCellTypesByViewType.get(viewHolder.getItemViewType());

        // Clustering and dates
        Cluster cluster = getClustering(message, position);
        if (cluster.mDateBoundaryWithPrevious) {
            Date sentAt = message.getSentAt();
            if (sentAt == null) sentAt = new Date();
            String timeBarDayText = Utils.formatTimeDay(sentAt);
            viewHolder.mTimeBarDay.setText(timeBarDayText);
            String timeBarTimeText = mTimeFormat.format(sentAt.getTime());
            viewHolder.mTimeBarTime.setText(timeBarTimeText);

            viewHolder.mTimeBar.setVisibility(View.VISIBLE);
            viewHolder.mSpaceMinute.setVisibility(View.GONE);
            viewHolder.mSpaceHour.setVisibility(View.GONE);
        } else {
            viewHolder.mTimeBar.setVisibility(View.GONE);
            if (cluster.mClusterWithPrevious != null) {
                switch (cluster.mClusterWithPrevious) {
                    case MINUTE:
                        viewHolder.mSpaceMinute.setVisibility(View.GONE);
                        viewHolder.mSpaceHour.setVisibility(View.GONE);
                        break;
                    case HOUR:
                        viewHolder.mSpaceMinute.setVisibility(View.VISIBLE);
                        viewHolder.mSpaceHour.setVisibility(View.GONE);
                        break;
                    case MORE_THAN_HOUR:
                        viewHolder.mSpaceMinute.setVisibility(View.VISIBLE);
                        viewHolder.mSpaceHour.setVisibility(View.VISIBLE);
                        break;
                    case NEW_SENDER:
                        viewHolder.mSpaceMinute.setVisibility(View.VISIBLE);
                        viewHolder.mSpaceHour.setVisibility(View.VISIBLE);
                        break;
                }
            } else {
                // No previous message
                viewHolder.mSpaceMinute.setVisibility(View.GONE);
                viewHolder.mSpaceHour.setVisibility(View.GONE);
            }
        }

        // Name (only if it's not mine and it's the first message in a cluster from a new sender)
        if (!cellType.mMe && (cluster.mClusterWithPrevious == null || cluster.mClusterWithPrevious == ClusterType.NEW_SENDER)) {
            Actor sender = message.getSender();
            if (sender.getName() != null) {
                viewHolder.mUserNameHeader.setText(sender.getName());
            } else {
                Participant participant = mParticipantProvider.getParticipant(sender.getUserId());
                viewHolder.mUserNameHeader.setText(participant != null ? participant.getName() : "...");
            }
            viewHolder.mUserNameHeader.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mUserNameHeader.setVisibility(View.GONE);
        }

        // Read and delivery receipts
        if (message == mReceiptMap.get(Message.RecipientStatus.READ)) {
            viewHolder.mReceipt.setVisibility(View.VISIBLE);
            viewHolder.mReceipt.setText("Read"); // TODO: add read-at time once available
        } else if (message == mReceiptMap.get(Message.RecipientStatus.DELIVERED)) {
            viewHolder.mReceipt.setVisibility(View.VISIBLE);
            viewHolder.mReceipt.setText("Delivered");
        } else {
            viewHolder.mReceipt.setVisibility(View.GONE);
        }

        // Unsent and sent
        if (message.isSent()) {
            viewHolder.mCellView.setAlpha(1.0f);
        } else {
            viewHolder.mCellView.setAlpha(0.5f);
        }

        // CellHolder
        AtlasCellFactory.CellHolder cellHolder = viewHolder.mCellHolder;
        cellHolder.setMessage(message);
        cellType.mCellFactory.bindCellHolder(cellHolder, message, cellType.mMe, position);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        boolean isMe = mLayerClient.getAuthenticatedUserId().equals(message.getSender().getUserId());
        for (AtlasCellFactory factory : mCellFactories) {
            if (!factory.isBindable(message)) continue;
            return isMe ? mMyViewTypesByCell.get(factory) : mTheirViewTypesByCell.get(factory);
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mQueryController.getItemCount();
    }

    public Integer getPosition(Message message) {
        return mQueryController.getPosition(message);
    }

    public Integer getPosition(Message message, int lastPosition) {
        return mQueryController.getPosition(message, lastPosition);
    }

    public Message getItem(int position) {
        return mQueryController.getItem(position);
    }


    //==============================================================================================
    // Clustering
    //==============================================================================================

    // TODO: limit only to visible positions and positions just-outside the visible range?
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

    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay() != d2.getDay());
    }

    private void requestUpdate(final Message message, final int lastPosition) {
        mUiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(getPosition(message, lastPosition));
            }
        });
    }


    //==============================================================================================
    // Read and delivery receipts
    //==============================================================================================

    // TODO: find clever way to prevent worst-case O(n) -- perhaps based on visible positions?
    private void updateReceipts() {
        final String userId = mLayerClient.getAuthenticatedUserId();
        Map<Message.RecipientStatus, Message> receiptMap = new HashMap<Message.RecipientStatus, Message>();
        for (int position = getItemCount(); position >= 0; position--) {
            Message message = getItem(position);
            if (message == null) continue;

            // Only display receipts for our own messages
            if (!userId.equals(message.getSender().getUserId())) continue;

            for (Map.Entry<String, Message.RecipientStatus> entry : message.getRecipientStatus().entrySet()) {
                // Only show receipts for other members
                if (entry.getKey().equals(userId)) continue;

                // Only the latest entry for this RecipientStatus matters
                if (receiptMap.containsKey(entry.getValue())) continue;

                // Found the latest entry for this RecipientStatus
                receiptMap.put(entry.getValue(), message);
            }
            if (receiptMap.containsKey(Message.RecipientStatus.READ) && receiptMap.containsKey(Message.RecipientStatus.DELIVERED)) {
                break;
            }
        }
        mReceiptMap = receiptMap;
    }


    //==============================================================================================
    // UI update callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        updateReceipts();
        notifyDataSetChanged();
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        updateReceipts();
        notifyItemChanged(position);
    }

    @Override
    public void onQueryItemRangeChanged(RecyclerViewController controller, int positionStart, int itemCount) {
        updateReceipts();
        notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        updateReceipts();
        notifyItemInserted(position);
        if (mAppendListener != null && (position + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(position));
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        updateReceipts();
        notifyItemRangeInserted(positionStart, itemCount);
        int positionEnd = positionStart + itemCount;
        if (mAppendListener != null && (positionEnd + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(positionEnd));
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        updateReceipts();
        notifyItemRemoved(position);
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart, int itemCount) {
        updateReceipts();
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition, int toPosition) {
        updateReceipts();
        notifyItemMoved(fromPosition, toPosition);
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Layouts to inflate
        public final static int RESOURCE_ID_ME = R.layout.atlas_message_item_me;
        public final static int RESOURCE_ID_THEM = R.layout.atlas_message_item_them;

        // View cache
        protected TextView mUserNameHeader;
        protected View mTimeBar;
        protected TextView mTimeBarDay;
        protected TextView mTimeBarTime;
        protected Space mSpaceMinute;
        protected Space mSpaceHour;
        protected ViewGroup mCellView;
        protected TextView mReceipt;

        // Cell
        protected AtlasCellFactory.CellHolder mCellHolder;

        public ViewHolder(View itemView) {
            super(itemView);

            mUserNameHeader = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_user_name);
            mTimeBar = itemView.findViewById(R.id.atlas_view_messages_convert_timebar);
            mTimeBarDay = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_timebar_day);
            mTimeBarTime = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_timebar_time);
            mSpaceMinute = (Space) itemView.findViewById(R.id.atlas_view_messages_convert_spacer_top_1);
            mSpaceHour = (Space) itemView.findViewById(R.id.atlas_view_messages_convert_spacer_top_2);
            mCellView = (ViewGroup) itemView.findViewById(R.id.atlas_view_messages_cell_container);
            mReceipt = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_delivery_receipt);
        }
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

    private static class CellType {
        protected final boolean mMe;
        protected final AtlasCellFactory mCellFactory;

        public CellType(boolean me, AtlasCellFactory CellFactory) {
            mMe = me;
            mCellFactory = CellFactory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CellType cellType = (CellType) o;

            if (mMe != cellType.mMe) return false;
            return mCellFactory.equals(cellType.mCellFactory);

        }

        @Override
        public int hashCode() {
            int result = (mMe ? 1 : 0);
            result = 31 * result + mCellFactory.hashCode();
            return result;
        }
    }

    /**
     * Listens for inserts to the end of an AtlasQueryAdapter.
     */
    public interface OnMessageAppendListener {
        /**
         * Alerts the listener to inserts at the end of an AtlasQueryAdapter.  If a batch of items
         * were appended, only the last one will be alerted here.
         *
         * @param adapter The AtlasQueryAdapter which had an item appended.
         * @param message The item appended to the AtlasQueryAdapter.
         */
        void onMessageAppend(AtlasMessagesAdapter adapter, Message message);
    }

    public interface OnMessageClickListener {
        void onMessageClick(AtlasMessagesAdapter adapter, Message message);

        boolean onMessageLongClick(AtlasMessagesAdapter adapter, Message message);
    }

}