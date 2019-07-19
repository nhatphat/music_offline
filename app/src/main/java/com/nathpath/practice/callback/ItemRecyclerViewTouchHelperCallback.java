package com.nathpath.practice.callback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.nathpath.practice.R;

public class ItemRecyclerViewTouchHelperCallback extends ItemTouchHelper.Callback {
    private ItemRecyclerViewTouchListener mCallback;
    private Context context;

    private Paint mClearPaint;
    private Drawable iconDel;
    private ColorDrawable background;
    private int intrinsicWidth = 0;
    private int intrinsicHeight = 0;

    public ItemRecyclerViewTouchHelperCallback(ItemRecyclerViewTouchListener mCallback, Context context) {
        this.mCallback = mCallback;
        this.context = context;

        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        background = new ColorDrawable();
        iconDel = ContextCompat.getDrawable(context, R.drawable.ic_delete_white);
        if (iconDel != null) {
            intrinsicHeight = iconDel.getIntrinsicHeight();
            intrinsicWidth = iconDel.getIntrinsicWidth();
        }
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int move = ItemTouchHelper.UP|ItemTouchHelper.DOWN;
        int swipe = ItemTouchHelper.END;
        return makeMovementFlags(move, swipe);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if(mCallback != null){
            mCallback.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if(mCallback != null){
            mCallback.onSwipe(viewHolder.getAdapterPosition(), direction);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();
        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;
        int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;

//        if(dX < 0) {
//            background.setBounds(
//                    itemView.getRight() + (int)(dX),
//                    itemView.getTop(),
//                    itemView.getRight(),
//                    itemView.getBottom()
//            );
//            background.setColor(Color.RED);
//            background.draw(c);
//
//
//            int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
//            int deleteIconRight = itemView.getRight() - deleteIconMargin;
//            iconDel.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
//            iconDel.draw(c);
//        }

        if(dX > 0) {
            background.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );
            background.setColor(Color.RED);
            background.draw(c);


            int deleteIconLeft = itemView.getLeft() + deleteIconMargin;
            int deleteIconRight = itemView.getLeft() + deleteIconMargin + intrinsicWidth;
            iconDel.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            iconDel.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
