package com.layer.atlas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.layer.atlas.simple.transformations.CircleCrop;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class AtlasAvatarCluster extends View {
    private static final String TAG = AtlasAvatarCluster.class.getSimpleName();

    private final static CircleCrop TRANSFORMATION = new CircleCrop();

    private static final Paint PAINT_TRANSPARENT = new Paint();
    private static final Paint PAINT_BITMAP = new Paint();
    private static final Paint PAINT_INITIALS = new Paint();
    private static final Paint PAINT_BORDER = new Paint();
    private static final Paint PAINT_BACKGROUND = new Paint();

    private static float mDensity;
    
    // TODO: make these stylable
    private static final float SINGLE_SIZE_DP = 40f;
    private static final float MULTI_SIZE_DP = 26f;
    private static final float BORDER_SIZE_DP = 0.5f;
    private static final float SINGLE_TEXT_SIZE_DP = 16f;
    private static final float MULTI_TEXT_SIZE_DP = SINGLE_TEXT_SIZE_DP * (MULTI_SIZE_DP / SINGLE_SIZE_DP);

    private float mInnerRadius;
    private float mOuterRadius;
    private float mTextSize;

    static {
        PAINT_TRANSPARENT.setARGB(0, 255, 255, 255);
        PAINT_TRANSPARENT.setAntiAlias(true);

        PAINT_BITMAP.setARGB(255, 255, 255, 255);
        PAINT_BITMAP.setAntiAlias(true);

        PAINT_INITIALS.setARGB(255, 0, 0, 0);
        PAINT_INITIALS.setAntiAlias(true);
        PAINT_INITIALS.setSubpixelText(true);

        PAINT_BORDER.setARGB(255, 255, 255, 255);
        PAINT_BORDER.setAntiAlias(true);

        PAINT_BACKGROUND.setARGB(255, 235, 235, 235);
        PAINT_BACKGROUND.setAntiAlias(true);
    }

    private final Map<String, ImageTarget> mImageTargets = new HashMap<String, ImageTarget>();
    private final Map<String, String> mInitials = new HashMap<String, String>();

    private Rect mTextRect = new Rect();
    private Rect mSourceRect = new Rect();
    private RectF mDestRect = new RectF();

    private ParticipantProvider mParticipantProvider;


    public AtlasAvatarCluster(Context context) {
        super(context);
    }

    public AtlasAvatarCluster(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtlasAvatarCluster(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AtlasAvatarCluster init(ParticipantProvider participantProvider) {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mParticipantProvider = participantProvider;
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int totalWidth = canvas.getWidth();
        int totalHeight = canvas.getHeight();

        int avatarCount = mInitials.size();
        canvas.drawRect(0f, 0f, totalWidth, totalHeight, PAINT_TRANSPARENT);
        if (avatarCount == 0) return;

        // Drawable size
        float drawableWidth = totalWidth - (getPaddingLeft() + getPaddingRight());
        float drawableHeight = totalHeight - (getPaddingTop() + getPaddingBottom());

        // Avatar size
        float outerWidth = mOuterRadius * 2;
        float cx = getPaddingLeft() + mOuterRadius;
        float cy = getPaddingTop() + mOuterRadius;
        float dx = (drawableWidth - outerWidth) / (avatarCount - 1);
        float dy = (drawableHeight - outerWidth) / (avatarCount - 1);

        for (Map.Entry<String, String> entry : mInitials.entrySet()) {
            ImageTarget imageTarget = mImageTargets.get(entry.getKey());
            boolean isImage = false;
            canvas.drawCircle(cx, cy, mOuterRadius, PAINT_BORDER);
            if (imageTarget != null) {
                Bitmap bitmap = imageTarget.getBitmap();
                if (bitmap != null) {
                    mSourceRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    mDestRect.set(cx - mInnerRadius, cy - mInnerRadius, cx + mInnerRadius, cy + mInnerRadius);
                    canvas.drawBitmap(bitmap, mSourceRect, mDestRect, PAINT_BITMAP);
                    isImage = true;
                }
            }
            if (!isImage) {
                String initials = entry.getValue();
                PAINT_INITIALS.setTextSize(mTextSize);
                PAINT_INITIALS.getTextBounds(initials, 0, initials.length(), mTextRect);
                canvas.drawCircle(cx, cy, mInnerRadius, PAINT_BACKGROUND);
                canvas.drawText(initials, cx - mTextRect.centerX(), cy - mTextRect.centerY() - 1f, PAINT_INITIALS);
            }

            cx += dx;
            cy += dy;
        }
    }

    public AtlasAvatarCluster setParticipants(Set<String> participantIds) {
        List<String> addParticipants = new ArrayList<String>(participantIds.size());
        List<String> removeParticipants = new ArrayList<String>(mImageTargets.size());
        for (String participantId : participantIds) {
            if (!mImageTargets.containsKey(participantId)) addParticipants.add(participantId);
        }
        for (String participantId : mImageTargets.keySet()) {
            if (!participantIds.contains(participantId)) removeParticipants.add(participantId);
        }
        if (addParticipants.isEmpty() && removeParticipants.isEmpty()) return this;

        List<ImageTarget> toLoad = new ArrayList<ImageTarget>(participantIds.size());
        int recycled = 0;
        for (String participantId : addParticipants) {
            Participant participant = mParticipantProvider.getParticipant(participantId);
            if (participant == null) {
                mInitials.remove(participantId);
                mImageTargets.remove(participantId);
                continue;
            }
            mInitials.put(participantId, initials(participant.getName()));
            if (recycled < removeParticipants.size()) {
                // Recycle an ImageTarget
                ImageTarget recycledTarget = mImageTargets.remove(removeParticipants.get(recycled++));
                mImageTargets.put(participantId, recycledTarget);
                recycledTarget.setUrl(participant.getAvatarUrl());
                toLoad.add(recycledTarget);
            } else {
                // Add a new ImageTarget
                ImageTarget newTarget = new ImageTarget(this);
                mImageTargets.put(participantId, newTarget);
                newTarget.setUrl(participant.getAvatarUrl());
                toLoad.add(newTarget);
            }
        }

        while (recycled < removeParticipants.size()) {
            String participantId = removeParticipants.get(recycled++);
            mInitials.remove(participantId);
            mImageTargets.remove(participantId);
        }

        int avatarCount = mInitials.size();
        if (avatarCount == 0) return this;
        mOuterRadius = (mDensity * ((avatarCount == 1) ? SINGLE_SIZE_DP : MULTI_SIZE_DP)) / 2f;
        mInnerRadius = mOuterRadius - 2f * mDensity * BORDER_SIZE_DP;
        mTextSize = (avatarCount == 1) ? (mDensity * SINGLE_TEXT_SIZE_DP) : (mDensity * MULTI_TEXT_SIZE_DP);

        Picasso picasso = Picasso.with(getContext());
        int size = (int) (mInnerRadius * 2f);
        for (ImageTarget imageTarget : toLoad) {
            picasso.load(imageTarget.getUrl()).centerCrop().resize(size, size)
                    .transform(TRANSFORMATION).into(imageTarget);
        }

        invalidate();
        return this;
    }

    public static String initials(String s) {
        return ("" + s.charAt(0)).toUpperCase();
    }

    private static class ImageTarget implements Target {
        private final static AtomicLong sCounter = new AtomicLong(0);
        private final long mId;
        private final AtlasAvatarCluster mCluster;
        private String mUrl;
        private Bitmap mBitmap;

        public ImageTarget(AtlasAvatarCluster cluster) {
            mId = sCounter.incrementAndGet();
            mCluster = cluster;
        }

        public void setUrl(String url) {
            mUrl = url;
        }

        public String getUrl() {
            return mUrl;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mCluster.invalidate();
            mBitmap = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mCluster.invalidate();
            mBitmap = null;
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            mBitmap = null;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImageTarget target = (ImageTarget) o;
            return mId == target.mId;
        }

        @Override
        public int hashCode() {
            return (int) (mId ^ (mId >>> 32));
        }
    }
}
