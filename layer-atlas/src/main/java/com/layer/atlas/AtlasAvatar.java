package com.layer.atlas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.layer.atlas.simple.transformations.SimpleCircleCrop;
import com.layer.sdk.messaging.Actor;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class AtlasAvatar implements Target {
    private static final String TAG = AtlasAvatar.class.getSimpleName();

    private final static SimpleCircleCrop sCircleCrop = new SimpleCircleCrop();

    private final ParticipantProvider mParticipantProvider;
    private final Context mContext;
    private final TextView mInitials;
    private final ImageView mImage;

    public AtlasAvatar(Context context, ParticipantProvider participantProvider, TextView initials, ImageView image) {
        mContext = context;
        mParticipantProvider = participantProvider;
        mInitials = initials;
        mImage = image;
    }

    public void setActor(final Actor actor) {
        final String initials;
        final Participant participant;
        if (actor.getUserId() == null) {
            participant = null;
            initials = ("" + actor.getName().charAt(0)).toUpperCase();
        } else {
            participant = mParticipantProvider.getParticipant(actor.getUserId());
            initials = ("" + participant.getName().charAt(0)).toUpperCase();
        }
        mInitials.setText(initials);
        mImage.setVisibility(View.GONE);
        mInitials.setVisibility(View.VISIBLE);

        if (participant != null) {
            Picasso.with(mContext)
                    .load(participant.getAvatarUrl()) // use HTTP cache
                    .resizeDimen(R.dimen.atlas_message_item_avatar, R.dimen.atlas_message_item_avatar)
                    .centerInside()
                    .placeholder(R.drawable.atlas_shape_circle_gray)
                    .transform(sCircleCrop)
                    .into(this);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mImage.setImageBitmap(bitmap);
        mInitials.setVisibility(View.GONE);
        mImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        mImage.setVisibility(View.GONE);
        mInitials.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        // Do nothing
    }
}
