package com.andtinder.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.andtinder.R;
import com.andtinder.model.CardModel;

import java.util.ArrayList;
import java.util.Collection;

public abstract class CardStackAdapter extends BaseCardStackAdapter {
	private final Context mContext;

	/**
	 * Lock used to modify the content of {@link #mData}. Any write operation
	 * performed on the deque should be synchronized on this lock.
	 */
	private final Object mLock = new Object();
	private ArrayList<CardModel> mData;

    private OnItemAddRemoveListener mItemAddRemoveListener = null;

	public CardStackAdapter(Context context) {
		mContext = context;
		mData = new ArrayList<CardModel>();
	}

	public CardStackAdapter(Context context, Collection<? extends CardModel> items) {
		mContext = context;
		mData = new ArrayList<CardModel>(items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FrameLayout wrapper = (FrameLayout) convertView;
		FrameLayout innerWrapper;
		View cardView;
		View convertedCardView;
		if (wrapper == null) {
			wrapper = new FrameLayout(mContext);
			wrapper.setBackgroundResource(R.drawable.card_bg);
			if (shouldFillCardBackground()) {
				innerWrapper = new FrameLayout(mContext);
				innerWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.card_bg));
				wrapper.addView(innerWrapper);
			} else {
				innerWrapper = wrapper;
			}
			cardView = getCardView(position, getCardModel(position), null, parent);
			innerWrapper.addView(cardView);
		} else {
			if (shouldFillCardBackground()) {
				innerWrapper = (FrameLayout) wrapper.getChildAt(0);
			} else {
				innerWrapper = wrapper;
			}
			cardView = innerWrapper.getChildAt(0);
			convertedCardView = getCardView(position, getCardModel(position), cardView, parent);
			if (convertedCardView != cardView) {
				wrapper.removeView(cardView);
				wrapper.addView(convertedCardView);
			}
		}

		return wrapper;
	}

	protected abstract View getCardView(int position, CardModel model, View convertView, ViewGroup parent);

	public boolean shouldFillCardBackground() {
		return true;
	}

	public void add(CardModel item) {
		synchronized (mLock) {
			mData.add(item);
		}
		notifyDataSetChanged();
        if (mItemAddRemoveListener != null) {
            mItemAddRemoveListener.onItemAdd(item, mData.size() - 1);
        }
	}

	public CardModel pop() {
		CardModel model;
        int position;
		synchronized (mLock) {
            position = mData.size() - 1;
			model = mData.remove(position);
		}
		notifyDataSetChanged();
        if (mItemAddRemoveListener != null) {
            mItemAddRemoveListener.onItemRemove(model, position);
        }
		return model;
	}

	@Override
	public Object getItem(int position) {
		return getCardModel(position);
	}

	public CardModel getCardModel(int position) {
		synchronized (mLock) {
			return mData.get(mData.size() - 1 - position);
		}
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	public Context getContext() {
		return mContext;
	}

    public void setItemAddRemoveListener(OnItemAddRemoveListener itemAddRemoveListener) {
        mItemAddRemoveListener = itemAddRemoveListener;
    }

    public interface OnItemAddRemoveListener {
        void onItemAdd(CardModel cardModel, int position);
        void onItemRemove(CardModel cardModel, int position);
    }
}
