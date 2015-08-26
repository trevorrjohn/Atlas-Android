package com.layer.atlas.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.layer.atlas.R;

class MessageViewHolder extends RecyclerView.ViewHolder {
    // View cache
    protected TextView mUserNameHeader;
    protected View mTimeBar;
    protected TextView mTimeBarDay;
    protected TextView mTimeBarTime;
    protected Space mSpaceMinute;
    protected Space mSpaceHour;
    protected ViewGroup mCellView;

    // Cell
    protected CellHolder mCellHolder;

    public MessageViewHolder(View itemView) {
        super(itemView);

        mUserNameHeader = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_user_name);
        mTimeBar = itemView.findViewById(R.id.atlas_view_messages_convert_timebar);
        mTimeBarDay = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_timebar_day);
        mTimeBarTime = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_timebar_time);
        mSpaceMinute = (Space) itemView.findViewById(R.id.atlas_view_messages_convert_spacer_top_1);
        mSpaceHour = (Space) itemView.findViewById(R.id.atlas_view_messages_convert_spacer_top_2);
        mCellView = (ViewGroup) itemView.findViewById(R.id.atlas_view_messages_cell_container);
    }
}
