package com.puzzle.game;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class TouchListener implements View.OnTouchListener {

    private float xDelta;
    private float yDelta;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();
        final double tolerance = sqrt(pow(view.getWidth(), 2) + pow(view.getHeight(), 2)) / 10;

        PuzzlePiece piece = (PuzzlePiece) view;
        if (!piece.canMove) {
            return true;
        }

        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        View parentView = (View) view.getParent();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDelta = x - lParams.leftMargin;
                yDelta = y - lParams.topMargin;
                piece.bringToFront();
                break;

            case MotionEvent.ACTION_MOVE:

                if ((x - xDelta) + piece.pieceWidth < parentView.getWidth()) {
                    lParams.leftMargin = (int) (x - xDelta);
                } else{
                    lParams.leftMargin = parentView.getWidth() - piece.pieceWidth;
                }

                if ((y - yDelta) + piece.pieceHeight < parentView.getHeight()) {
                    lParams.topMargin = (int) (y - yDelta);
                } else {
                    lParams.topMargin = parentView.getHeight() - piece.pieceHeight;
                }
                if (lParams.leftMargin < 0) {
                    lParams.leftMargin = 0;
                }

                if (lParams.topMargin < 0) {
                    lParams.topMargin = 0;
                }
                view.setLayoutParams(lParams);
                break;
            case MotionEvent.ACTION_UP:

                int xDiff = abs(piece.xCoord - lParams.leftMargin);
                int yDiff = abs(piece.yCoord - lParams.topMargin);

                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = piece.xCoord;
                    lParams.topMargin = piece.yCoord;
                    piece.setLayoutParams(lParams);
                    piece.canMove = false;
                    sendViewToBack(piece);
                }
                break;
        }

        return true;
    }

    public void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    public boolean isDraggable(PuzzlePiece piece, int changedWidth, int changedHeight) {
        if (changedWidth < piece.pieceWidth || changedHeight < piece.pieceHeight)
            return false;
        return true;
    }
}