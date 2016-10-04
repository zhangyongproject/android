package com.cyjh.screenmirror;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.os.SystemClock;
//import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

@SuppressLint("NewApi") 
public class EventInjector {

	/*Key, buttons*/
	private static void injectKeyEvent(InputManager im, Method method, KeyEvent event) 
			throws InvocationTargetException, IllegalAccessException
	{
		method.invoke(im, new Object[]{ event, Integer.valueOf(1)});
	} 
	
	/*Mouse Touch
	 * See frameworks/native/include/android/input.h
	 * AINPUT_SOURCE_CLASS_NONE = 0x00000000,
     * AINPUT_SOURCE_CLASS_BUTTON = 0x00000001,
     * AINPUT_SOURCE_CLASS_POINTER = 0x00000002,
     * AINPUT_SOURCE_CLASS_NAVIGATION = 0x00000004,
     * AINPUT_SOURCE_CLASS_POSITION = 0x00000008,
     * AINPUT_SOURCE_CLASS_JOYSTICK = 0x00000010,
     *   
     * AINPUT_SOURCE_KEYBOARD = 0x00000100 | AINPUT_SOURCE_CLASS_BUTTON,
     * AINPUT_SOURCE_DPAD = 0x00000200 | AINPUT_SOURCE_CLASS_BUTTON,
     * AINPUT_SOURCE_GAMEPAD = 0x00000400 | AINPUT_SOURCE_CLASS_BUTTON,
     * AINPUT_SOURCE_TOUCHSCREEN = 0x00001000 | AINPUT_SOURCE_CLASS_POINTER,
     * AINPUT_SOURCE_MOUSE = 0x00002000 | AINPUT_SOURCE_CLASS_POINTER,
     * AINPUT_SOURCE_STYLUS = 0x00004000 | AINPUT_SOURCE_CLASS_POINTER,
     * AINPUT_SOURCE_TRACKBALL = 0x00010000 | AINPUT_SOURCE_CLASS_NAVIGATION,
     * AINPUT_SOURCE_TOUCHPAD = 0x00100000 | AINPUT_SOURCE_CLASS_POSITION,
     * AINPUT_SOURCE_TOUCH_NAVIGATION = 0x00200000 | AINPUT_SOURCE_CLASS_NONE,
     * AINPUT_SOURCE_JOYSTICK = 0x01000000 | AINPUT_SOURCE_CLASS_JOYSTICK,
	 */
	private static void injectMotionEvent(InputManager im, Method method, int inputSource, int action,
			long when, float x, float y, float pressure)
			throws InvocationTargetException, IllegalAccessException
	{	
		MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
		event.setSource(inputSource);
		method.invoke(method, im, new Object[]{event, Integer.valueOf(0)});
	}
	
	/*
	 * Ref: KeycodeLabels.h
	 * */
	private static void sendKeyEvent(InputManager im, Method method, int source, int keyCode, boolean shift)
			throws InvocationTargetException,  IllegalAccessException
	{
		long now = SystemClock.uptimeMillis();
		if(shift) {
			injectKeyEvent(im, method, new KeyEvent(now, now, 0, keyCode, 0, 1, -1, 0, 0));
			injectKeyEvent(im, method, new KeyEvent(now, now, 1, keyCode, 0, 1, -1, 0, 0));
		}else {
			injectKeyEvent(im, method, new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0));
			injectKeyEvent(im, method, new KeyEvent(now, now, 0, keyCode, 0, 0, -1, 0, 0));
		}
	}
	
	@SuppressWarnings("unused")
	private static void  turnScreenOn(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException, RemoteException
	{
		sendKeyEvent(im, method, 0x101, 0x1a, false);
	}
	
	public static void mousemove(InputManager im, Method method, int x, int y) 
		throws InvocationTargetException, IllegalAccessException
	{
		injectMotionEvent(im, method, 0x1002, 2, SystemClock.uptimeMillis(), x, y, 1.0f);
	}
	
	public static void mouseup(InputManager im, Method method, int x, int y)
		throws InvocationTargetException, IllegalAccessException
	{
		injectMotionEvent(im, method, 0x1002, 1, SystemClock.uptimeMillis(), x, y, 1.0f);
	}
	
	public static void mousedown(InputManager im, Method method, int x, int y)
		throws InvocationTargetException, IllegalAccessException
	{
		injectMotionEvent(im, method, 0x1002, 0, SystemClock.uptimeMillis(), x, y, 1.0f);
	}
	
	public static void rotate()
	{
		//Todo
	}
	
	public static void scroll(InputManager im, Method method, float x, float y, int deltaX, int deltaY)
		throws InvocationTargetException, IllegalAccessException
	{
//		final float DEFAULT_SIZE = 1.0f;
//		final int DEFAULT_META_STATE = 0;
//		final int DEFAULT_BUTTON_STATE = 0;
//		final int DEFAULT_FLAGS = 0;
//		final float DEFALUT_PRECISION_X = 1.0f;
//		final float DEFAULT_PRECISION_Y = 1.0f;
//		final int DEFAULT_DEVICE_ID = 0;
//		final int DEFAULT_EDGE_FLAGS = 0;
		
		long when = SystemClock.uptimeMillis();
		
		MotionEvent.PointerProperties pp[] = new MotionEvent.PointerProperties[1];
		pp[0] = new MotionEvent.PointerProperties();
		pp[0].clear();
		pp[0].id = 0;
		MotionEvent.PointerCoords pc[] = new MotionEvent.PointerCoords[1];
		pc[0] = new MotionEvent.PointerCoords();
		pc[0].clear();
		pc[0].x = x;
		pc[0].y = y;
		pc[0].pressure = 1.0f;
		pc[0].size = 1.0f;
		pc[0].setAxisValue(10, deltaX);
		pc[0].setAxisValue(9, deltaY);
		MotionEvent event = MotionEvent.obtain(when, when, 8, 1, pp, pc, 0, 0, 1.0f, 1.0f, 0, 0, 0x1002, 0);
		
		method.invoke(im,  new Object[]{event, Integer.valueOf(0)});
		
	}
	
	public static void home(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 3, false);
	}
	
	public static void backspace(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 0x43, false);
	}
	
	public static void down(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 0x4, false);
	}
	
	public static void up(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 0x13, false);
	}
	
	public static void left(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 0x15, false);
	}
	
	public static void right(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101,0x16, false);
	}
	
	public static void back(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 4, false);
	}
	
	public static void menu(InputManager im, Method method)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, 0x52, false);
	}
	
	public static void keycode(InputManager im, Method method, int keycode, boolean shift)
		throws InvocationTargetException, IllegalAccessException
	{
		sendKeyEvent(im, method, 0x101, keycode, shift);
	}
	
	public static void keychar(InputManager im, Method method, String keychar)
		throws InvocationTargetException, IllegalAccessException
	{
	
	}
		
}
