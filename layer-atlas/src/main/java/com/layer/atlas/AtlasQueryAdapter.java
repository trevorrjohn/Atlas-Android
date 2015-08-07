package com.layer.atlas;

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
    private final Callback mCallback;

    public AtlasQueryAdapter(LayerClient client, Query<Tquery> query, Callback callback) {
        mQueryController = client.newRecyclerViewController(query, null, this);
        mCallback = callback;
        setHasStableIds(false);
    }

    /**
     * Returns a ViewHolder for the given view type, to be bound with data later in {@link #onBindViewHolder(RecyclerView.ViewHolder, Queryable)}.
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
     */
    public abstract void onBindViewHolder(Tview viewHolder, Tquery queryable);

    /**
     * Override this method if more than one view type is used.
     *
     * @param queryable Queryable to get the view type for.
     * @return Integer representing the view type of the given Queryable.
     */
    public int getItemViewType(Tquery queryable) {
        return 1;
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

    @Override
    public void onBindViewHolder(Tview viewHolder, int position) {
        onBindViewHolder(viewHolder, mQueryController.getItem(position));
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(mQueryController.getItem(position));
    }

    @Override
    public int getItemCount() {
        return mQueryController.getItemCount();
    }

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
        if ((position + 1) == getItemCount()) {
            mCallback.onItemInserted();
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
        if ((positionStart + itemCount + 1) == getItemCount()) {
            mCallback.onItemInserted();
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

    public interface Callback {
        void onItemInserted();
    }

}