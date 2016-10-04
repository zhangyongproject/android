package com.cyjh.library;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

@SuppressLint("Recycle")
public class Touchpad {
    private class Point {
        private final PointerProperties properties = new PointerProperties();
        private final PointerCoords coords = new PointerCoords();
        private long time;

        public Point(int finger, int x, int y) {
            properties.toolType = MotionEvent.TOOL_TYPE_FINGER;
            properties.id = finger;
            coords.x = x;
            coords.y = y;
            time = SystemClock.uptimeMillis();
        }
    }

    private List<Point> mPoints = new ArrayList<Point>();
    private List<MotionEvent> mEvents = new ArrayList<MotionEvent>();

    public Point onTouch(int finger, int action, int x, int y) {
        long when = SystemClock.uptimeMillis();
        int source = InputDevice.SOURCE_TOUCHSCREEN;
        int device = 6;
        int count;
        PointerProperties[] properties;
        PointerCoords[] coords;
        Point point = getPoint(finger);
        if (action == MotionEvent.ACTION_DOWN) {
            if (point == null) {
                point = new Point(finger, x, y);
                point.coords.x = x;
                point.coords.y = y;
                if (mPoints.isEmpty()) {
                    action = MotionEvent.ACTION_DOWN;
                    mPoints.add(point);
                } else {
                    action = MotionEvent.ACTION_POINTER_DOWN;
                    mPoints.add(point);
                    action |= (mPoints.size() - 1) << 8;
                }
                count = mPoints.size();
                properties = getPointerProperties();
                coords = getPointerCoords();
                mEvents.add(MotionEvent.obtain(point.time, when, action, count, properties, coords, 0, 0, 1, 1, device, 0, source, 0));
            } else {
                onTouch(finger, MotionEvent.ACTION_MOVE, x, y);
            }
            return point;
        } else if (action == MotionEvent.ACTION_UP) {
            if (point == null) {
                point = onTouch(finger, MotionEvent.ACTION_DOWN, x, y);
            }
            point.coords.x = x;
            point.coords.y = y;
            count = mPoints.size();
            if (count == 1) {
                action = MotionEvent.ACTION_UP;
            } else {
                if (mPoints.get(mPoints.size() - 1) == point) {
                    action = MotionEvent.ACTION_POINTER_UP;
                    action |= (mPoints.size() - 1) << 8;
                } else {
                    action = MotionEvent.ACTION_POINTER_UP;
                }
            }
            properties = getPointerProperties();
            coords = getPointerCoords();
            mPoints.remove(point);
            mEvents.add(MotionEvent.obtain(point.time, when, action, count, properties, coords, 0, 0, 1, 1, device, 0, source, 0));
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (point == null) {
                point = onTouch(finger, MotionEvent.ACTION_DOWN, x, y);
            }
            point.coords.x = x;
            point.coords.y = y;
            count = mPoints.size();
            properties = getPointerProperties();
            coords = getPointerCoords();
            mEvents.add(MotionEvent.obtain(point.time, when, action, count, properties, coords, 0, 0, 1, 1, device, 0, source, 0));
        }
        return point;
    }

    private Point getPoint(int finger) {
        for (Point point : mPoints) {
            if (point.properties.id == finger) {
                return point;
            }
        }
        return null;
    }

    private PointerProperties[] getPointerProperties() {
        int size = mPoints.size();
        PointerProperties[] properties = new PointerProperties[size];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = mPoints.get(i).properties;
        }
        return properties;
    }

    private PointerCoords[] getPointerCoords() {
        int size = mPoints.size();
        PointerCoords[] coords = new PointerCoords[size];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = mPoints.get(i).coords;
        }
        return coords;
    }

    public boolean has(int finger) {
        for (Point point : mPoints) {
            if (point.properties.id == finger) {
                return true;
            }
        }
        return false;
    }

    public List<MotionEvent> getEvents() {
        return mEvents;
    }
}
