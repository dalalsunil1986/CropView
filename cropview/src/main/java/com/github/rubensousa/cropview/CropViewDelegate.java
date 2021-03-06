package com.github.rubensousa.cropview;


import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

class CropViewDelegate implements View.OnTouchListener {

    private int lastX;
    private int lastY;
    private int currentEdge;
    private float touchRadius;
    private int minHeight;
    private int minWidth;
    private CropView cropView;
    private Rect previousRect;
    private Rect cropRect;
    private List<CropView.OnCropSectionChangeListener> listeners;

    public CropViewDelegate(CropView cropView, int touchRadius, int minWidth, int minHeight) {
        this.cropView = cropView;
        this.touchRadius = touchRadius;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.cropRect = new Rect();
        this.listeners = new ArrayList<>();
    }

    public void addOnCropSectionChangeListener(CropView.OnCropSectionChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeOnCropSectionChangeListener(CropView.OnCropSectionChangeListener listener) {
        listeners.remove(listener);
    }

    public void setCropRect(Rect rect) {
        cropRect = rect;
        cropView.invalidate();
    }

    public Rect getCropRect() {
        return cropRect;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                previousRect = new Rect(cropRect);
                return onTouchDown(event);
            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                return true;
        }
        return false;
    }

    private boolean onTouchDown(MotionEvent event) {
        if (isTouchingEdge(event, MotionEvent.EDGE_LEFT)) {
            currentEdge = MotionEvent.EDGE_LEFT;
        } else if (isTouchingEdge(event, MotionEvent.EDGE_TOP)) {
            currentEdge = MotionEvent.EDGE_TOP;
        } else if (isTouchingEdge(event, MotionEvent.EDGE_RIGHT)) {
            currentEdge = MotionEvent.EDGE_RIGHT;
        } else if (isTouchingEdge(event, MotionEvent.EDGE_BOTTOM)) {
            currentEdge = MotionEvent.EDGE_BOTTOM;
        } else {
            currentEdge = 0;
        }
        return currentEdge != 0 || isWithinBounds((int) event.getX(), (int) event.getY());
    }

    private void onTouchUp(MotionEvent event) {
        if (!previousRect.equals(cropRect)) {
            for (CropView.OnCropSectionChangeListener listener : listeners) {
                listener.onCropSectionChanged(cropRect);
            }
        }
        currentEdge = 0;
        lastY = 0;
        lastX = 0;
    }

    private void onTouchMove(MotionEvent event) {
        if (currentEdge == 0) {
            if (lastX == 0 || lastY == 0) {
                lastX = (int) event.getX();
                lastY = (int) event.getY();
            }
            int dx = (int) (event.getX() - lastX);
            int dy = (int) (event.getY() - lastY);
            if (cropRect.left + dx < 0) dx = 0;
            if (cropRect.top + dy < 0) dy = 0;
            if (cropRect.right + dx > cropView.getWidth()) dx = 0;
            if (cropRect.bottom + dy > cropView.getHeight()) dy = 0;
            cropRect.offset(dx, dy);
        } else {
            cropRect.left = getLeft(event);
            cropRect.right = getRight(event);
            cropRect.top = getTop(event);
            cropRect.bottom = getBottom(event);
        }
        cropView.invalidate();
        lastX = (int) event.getX();
        lastY = (int) event.getY();
    }

    private int getLeft(MotionEvent event) {
        int left = cropRect.left;
        if ((currentEdge == MotionEvent.EDGE_BOTTOM || currentEdge == MotionEvent.EDGE_LEFT)) {
            left = event.getX() < 0 ? 0 : (int) event.getX();
        }
        return cropRect.right - left > minWidth ? left : cropRect.left;
    }

    private int getRight(MotionEvent event) {
        int right = cropRect.right;
        if (currentEdge == MotionEvent.EDGE_TOP || currentEdge == MotionEvent.EDGE_RIGHT) {
            right = (int) event.getX() > cropView.getWidth()
                    ? cropView.getWidth() : (int) event.getX();
        }
        return right - cropRect.left > minWidth ? right : cropRect.right;
    }

    private int getTop(MotionEvent event) {
        int top = cropRect.top;
        if (currentEdge == MotionEvent.EDGE_LEFT || currentEdge == MotionEvent.EDGE_TOP) {
            top = (int) event.getY() < 0 ? 0 : (int) event.getY();
        }
        return cropRect.bottom - top > minHeight ? top : cropRect.top;
    }

    private int getBottom(MotionEvent event) {
        int bottom = cropRect.bottom;
        if (currentEdge == MotionEvent.EDGE_BOTTOM || currentEdge == MotionEvent.EDGE_RIGHT) {
            bottom = (int) event.getY() > cropView.getHeight()
                    ? cropView.getHeight() : (int) event.getY();
        }
        return bottom - cropRect.top > minHeight ? bottom : cropRect.bottom;
    }

    private boolean isTouchingEdge(MotionEvent event, int edge) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (edge) {
            case MotionEvent.EDGE_LEFT:
                return isWithinCircle(x, y, cropRect.left, cropRect.top, touchRadius);
            case MotionEvent.EDGE_TOP:
                return isWithinCircle(x, y, cropRect.right, cropRect.top, touchRadius);
            case MotionEvent.EDGE_RIGHT:
                return isWithinCircle(x, y, cropRect.right, cropRect.bottom, touchRadius);
            case MotionEvent.EDGE_BOTTOM:
                return isWithinCircle(x, y, cropRect.left, cropRect.bottom, touchRadius);
        }
        return false;
    }

    private boolean isWithinCircle(int x, int y, int x2, int y2, float radius) {
        return Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2)) <= radius;
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= cropRect.left && x <= cropRect.right
                && y <= cropRect.bottom && y >= cropRect.top;
    }
}
