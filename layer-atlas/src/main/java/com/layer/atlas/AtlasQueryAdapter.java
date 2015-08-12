package com.layer.atlas;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.Queryable;
import com.layer.sdk.query.RecyclerViewController;

/**
 * Wires a RecyclerViewController to a RecyclerView.  Extend this class with custom
 * RecyclerView.Adapters.
 */
public abstract class AtlasQueryAdapter<Tquery extends Queryable, Tview extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<Tview> implements RecyclerViewController.Callback {
    private final RecyclerViewController<Tquery> mQueryController;
    protected final Handler mUiThreadHandler;

    protected OnAppendListener<Tquery> mAppendListener;
    protected OnItemClickListener<Tquery> mItemClickListener;

    public AtlasQueryAdapter(LayerClient client) {
        // Setting Query to `null` means we must call setQuery() later.
        mQueryController = client.newRecyclerViewController(null, null, this);
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        setHasStableIds(false);
    }

    /**
     * Returns a ViewHolder for the given view type, to be bound with data later in {@link #onBindViewHolder(RecyclerView.ViewHolder, Queryable, int)}.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder for the given view type.
     */
    public abstract Tview onCreateViewHolder(ViewGroup parent, int viewType);

    /**
     * Called by the underlying RecyclerView to display data for the specified Queryable.  This
     * method should update the contents of the viewHolder to reflect that Queryable.
     *
     * @param viewHolder ViewHolder to bind with data from the given Queryable.
     * @param queryable  Queryable whose data to bind with the given ViewHolder.
     * @param position   Position of the Queryable within the Adapter.
     */
    public abstract void onBindViewHolder(Tview viewHolder, Tquery queryable, int position);

    /**
     * Override this method if more than one view type is used.
     *
     * @param queryable Queryable to get the view type for.
     * @return Integer representing the view type of the given Queryable.
     */
    public int getItemViewType(Tquery queryable) {
        return 1;
    }

    public AtlasQueryAdapter<Tquery, Tview> setQuery(Query<Tquery> query) {
        mQueryController.setQuery(query);
        return this;
    }

    /**
     * Refreshes this adapter by re-running the underlying Query.
     */
    public void refresh() {
        mQueryController.execute();
    }

    public Integer getPosition(Tquery queryable) {
        return mQueryController.getPosition(queryable);
    }

    public Integer getPosition(Tquery queryable, int lastPosition) {
        return mQueryController.getPosition(queryable, lastPosition);
    }

    public Tquery getItem(int position) {
        return mQueryController.getItem(position);
    }

    @Override
    public void onBindViewHolder(Tview viewHolder, int position) {
        onBindViewHolder(viewHolder, getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
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
    public AtlasQueryAdapter<Tquery, Tview> setOnAppendListener(OnAppendListener<Tquery> listener) {
        mAppendListener = listener;
        return this;
    }

    /**
     * Sets the OnItemClickListener for this AtlasQueryAdapter.  The listener will be called when
     * items are clicked.
     *
     * @param listener The OnItemClickListener to notify about item clicks.
     * @return This AtlasQueryAdapter.
     */
    public AtlasQueryAdapter<Tquery, Tview> setOnItemClickListener(OnItemClickListener<Tquery> listener) {
        mItemClickListener = listener;
        return this;
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

    /**
     * Listens inserts to the end of an AtlasQueryAdapter.
     *
     * @param <Tquery> Type of item the AtlasQueryAdapter contains.
     */
    public interface OnAppendListener<Tquery extends Queryable> {
        /**
         * Alerts the listener to inserts at the end of an AtlasQueryAdapter.  If a batch of items
         * were appended, only the last one will be alerted here.
         *
         * @param adapter The AtlasQueryAdapter which had an item appended.
         * @param item    The item appended to the AtlasQueryAdapter.
         */
        void onItemAppended(AtlasQueryAdapter<Tquery, ?> adapter, Tquery item);
    }

    /**
     * Listens for item clicks on an AtlasQueryAdapter.
     *
     * @param <Tquery> The type of item the AtlasQueryAdapter contains.
     */
    public interface OnItemClickListener<Tquery extends Queryable> {
        /**
         * Alerts the listener to item clicks.
         *
         * @param adapter The AtlasQueryAdapter which had an item clicked.
         * @param item    The item clicked.
         */
        void onItemClicked(AtlasQueryAdapter<Tquery, ?> adapter, Tquery item);

        /**
         * Alerts the listener to long item clicks.
         *
         * @param adapter The AtlasQueryAdapter which had an item long-clicked.
         * @param item    The item long-clicked.
         * @return true if the long-click was handled, false otherwise.
         */
        boolean onItemLongClicked(AtlasQueryAdapter<Tquery, ?> adapter, Tquery item);
    }
}