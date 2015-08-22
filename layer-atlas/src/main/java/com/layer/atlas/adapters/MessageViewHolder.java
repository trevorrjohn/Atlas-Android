package com.layer.atlas.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.layer.atlas.R;
import com.layer.sdk.messaging.Message;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    protected ViewGroup mCellView;
    protected Object mCellHolder;

    protected Message mMessage;

    public MessageViewHolder(View itemView) {
        super(itemView);
        mCellView = (ViewGroup) itemView.findViewById(R.id.atlas_view_messages_cell_container);
    }
}

//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
//        // Object this ViewHolder represents
//        protected Message mMessage;
//
//        // Cached View references
//        protected View mSpacerTop1;
//        protected View mSpacerTop2;
//        protected View mTimeBar;
//        protected TextView mTimeBarDay;
//        protected TextView mTimeBarTime;
//        protected View mAvatarContainer;
//        protected TextView mAvatarText;
//        protected ImageView mAvatarImage;
//        protected View mSpacerRight;
//        protected TextView mUserNameHeader;
//        protected ViewGroup mCellContainer;
//        protected TextView mReceiptView;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
//
//            mTimeBar = itemView.findViewById(R.id.atlas_view_messages_convert_timebar);
//            mTimeBarDay = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_timebar_day);
//            mTimeBarTime = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_timebar_time);
//
//            mSpacerTop1 = itemView.findViewById(R.id.atlas_view_messages_convert_spacer_top_1);
//            mSpacerTop2 = itemView.findViewById(R.id.atlas_view_messages_convert_spacer_top_2);
//
//            mAvatarContainer = itemView.findViewById(R.id.atlas_view_messages_convert_avatar_container);
//            mAvatarText = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_initials);
//            mAvatarImage = (ImageView) itemView.findViewById(R.id.atlas_view_messages_convert_avatar_img);
//
//            mSpacerRight = itemView.findViewById(R.id.atlas_view_messages_convert_spacer_right);
//
//            mUserNameHeader = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_user_name);
//
//            mCellContainer = (ViewGroup) itemView.findViewById(R.id.atlas_view_messages_cell_container);
//
//            mReceiptView = (TextView) itemView.findViewById(R.id.atlas_view_messages_convert_delivery_receipt);
//        }
//
//        @Override
//        public void onClick(View v) {
//            if (mItemClickListener == null) return;
//            mItemClickListener.onItemClicked(IntegrationMessagesAdapter.this, mMessage);
//        }
//
//        @Override
//        public boolean onLongClick(View v) {
//            if (mItemClickListener == null) return false;
//            return mItemClickListener.onItemLongClicked(IntegrationMessagesAdapter.this, mMessage);
//        }
//    }
