package com.nathpath.practice.callback;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeTouchListener implements View.OnTouchListener {
    private GestureDetector gestureDetector;

    public SwipeTouchListener(Context context){
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void onSwipeTop(){}
    public void onSwipeBottom(){}
    public void onSwipeLeft(){}
    public void onSwipeRight(){}

    class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if(diffX == 0 || diffY == 0){
               return false;
            }

            if(diffX > 0){
                onSwipeRight();
            }else if(diffX < 0){
                onSwipeLeft();
            }

            if(diffY > 0){
                onSwipeBottom();
            }else if(diffY < 0){
                onSwipeTop();
            }

            return true;
        }
    }
}
