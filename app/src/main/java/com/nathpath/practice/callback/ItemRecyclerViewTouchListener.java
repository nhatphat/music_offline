package com.nathpath.practice.callback;

public interface ItemRecyclerViewTouchListener {
    void onSwipe(int position, int direction);
    void onMove(int startPosition, int endPosition);
}
