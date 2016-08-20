package com.codepath.videotabletest;

import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by mfdavis on 7/20/16.
 */
public class ItemClickSupport {
    private final HorizontalGridView mHorizontalGridView;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                // ask the RecyclerView for the viewHolder of this view.
                // then use it to get the position for the adapter
                HorizontalGridView.ViewHolder holder = mHorizontalGridView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mHorizontalGridView, holder.getAdapterPosition(), v);
            }
        }
    };
    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnItemLongClickListener != null) {
                RecyclerView.ViewHolder holder = mHorizontalGridView.getChildViewHolder(v);
                return mOnItemLongClickListener.onItemLongClicked(mHorizontalGridView, holder.getAdapterPosition(), v);
            }
            return false;
        }
    };
    private RecyclerView.OnChildAttachStateChangeListener mAttachListener
            = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            // every time a new child view is attached add click listeners to it
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    private ItemClickSupport(HorizontalGridView horizontalGridView) {
        mHorizontalGridView = horizontalGridView;
        // the ID must be declared in XML, used to avoid
        // replacing the ItemClickSupport without removing
        // the old one from the HorizontalGridView
        mHorizontalGridView.setTag(R.id.item_click_support, this);
        mHorizontalGridView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static ItemClickSupport addTo(HorizontalGridView view) {
        // if there's already an ItemClickSupport attached
        // to this HorizontalGridView do not replace it, use it
        ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.item_click_support);
        if (support == null) {
            support = new ItemClickSupport(view);
        }
        return support;
    }

    public static ItemClickSupport removeFrom(HorizontalGridView view) {
        ItemClickSupport support = (ItemClickSupport) view.getTag(R.id.item_click_support);
        if (support != null) {
            support.detach(view);
        }
        return support;
    }

    public ItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public ItemClickSupport setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
        return this;
    }

    private void detach(HorizontalGridView view) {
        view.removeOnChildAttachStateChangeListener(mAttachListener);
        view.setTag(R.id.item_click_support, null);
    }

    public interface OnItemClickListener {

        void onItemClicked(HorizontalGridView horizontalGridView, int position, View v);
    }

    public interface OnItemLongClickListener {

        boolean onItemLongClicked(HorizontalGridView horizontalGridView, int position, View v);
    }
}
