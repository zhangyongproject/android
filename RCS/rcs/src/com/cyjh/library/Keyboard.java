package com.cyjh.library;

import java.util.ArrayList;
import java.util.List;

import android.os.SystemClock;
import android.util.SparseLongArray;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

public class Keyboard {

    private class Meta {

        public int meta;
        public int flag;

    }

    private final SparseLongArray mDownTime = new SparseLongArray();

    private boolean mKeyDownAlt;
    private boolean mKeyDownCtrl;
    private boolean mKeyDownMeta;
    private boolean mKeyDownShift;
    private boolean mCapsLockEnabled;

    public Meta getMeta(int key) {
        Meta meta = new Meta();
        if (mKeyDownShift) {
            if (isLetterKey(key)) {
                if (mCapsLockEnabled) {
                    meta.meta = meta.meta | 0;
                } else {
                    meta.meta = meta.meta | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
                }
            } else {
                meta.meta = meta.meta | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
            }
        } else {
            if (isLetterKey(key)) {
                if (mCapsLockEnabled) {
                    meta.meta = meta.meta | KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON;
                    meta.flag = 1;
                } else {
                    meta.meta = meta.meta | 0;
                }
            } else {
                meta.meta = meta.meta | 0;
            }
        }
        if (mKeyDownCtrl) {
            meta.meta = meta.meta | KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        }
        if (mKeyDownMeta) {
            meta.meta = meta.meta | KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON;
        }
        if (mKeyDownAlt) {
            meta.meta = meta.meta | KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
        }
        return meta;
    }

    private boolean isLetterKey(int key) {
        return key >= KeyEvent.KEYCODE_A && key <= KeyEvent.KEYCODE_Z;
    }

    public void setCapsLockEnabled(boolean value) {
        mCapsLockEnabled = value;
    }

    public List<KeyEvent> getEvents(int action, int code) {
        long now = SystemClock.uptimeMillis();
        int source = InputDevice.SOURCE_KEYBOARD;
        int device = KeyCharacterMap.VIRTUAL_KEYBOARD;
        Meta meta = getMeta(code);
        List<KeyEvent> events = new ArrayList<KeyEvent>();
        if (action == KeyEvent.ACTION_DOWN) {
            mDownTime.put(code, now);
            if (meta.flag == 1) {
                events.add(new KeyEvent(now, now, action, KeyEvent.KEYCODE_SHIFT_LEFT, 0, meta.meta, device, 0, 0, source));
                events.add(new KeyEvent(now, now, action, code, 0, meta.meta, device, 0, 0, source));
            } else {
                events.add(new KeyEvent(now, now, action, code, 0, meta.meta, device, 0, 0, source));
            }
            switch (code) {
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                mKeyDownShift = true;
                break;
            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                mKeyDownCtrl = true;
                break;
            case KeyEvent.KEYCODE_META_LEFT:
            case KeyEvent.KEYCODE_META_RIGHT:
                mKeyDownMeta = true;
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                mKeyDownAlt = true;
                break;
            default:
                break;
            }
        } else if (action == KeyEvent.ACTION_UP) {
            long time = mDownTime.get(code, now);
            mDownTime.delete(code);
            if (meta.flag == 1) {
                events.add(new KeyEvent(time, now, action, code, 0, meta.meta, device, 0, 0, source));
                events.add(new KeyEvent(time, now, action, KeyEvent.KEYCODE_SHIFT_LEFT, 0, meta.meta, device, 0, 0, source));
            } else {
                events.add(new KeyEvent(time, now, action, code, 0, meta.meta, device, 0, 0, source));
            }
            switch (code) {
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                mKeyDownShift = false;
                break;
            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT:
                mKeyDownCtrl = false;
                break;
            case KeyEvent.KEYCODE_META_LEFT:
            case KeyEvent.KEYCODE_META_RIGHT:
                mKeyDownMeta = false;
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                mKeyDownAlt = false;
                break;
            default:
                break;
            }
        }
        return events;
    }

    public static int getAndroidKeyCodeFromCSharp(int code) {
        switch (code) {
        case 27:// esc
            return KeyEvent.KEYCODE_ESCAPE;
        case 112:// f1
            return KeyEvent.KEYCODE_F1;
        case 113:// f2
            return KeyEvent.KEYCODE_F2;
        case 114:// f3
            return KeyEvent.KEYCODE_F3;
        case 115:// f4
            return KeyEvent.KEYCODE_F4;
        case 116:// f5
            return KeyEvent.KEYCODE_F5;
        case 117:// f6
            return KeyEvent.KEYCODE_F6;
        case 118:// f7
            return KeyEvent.KEYCODE_F7;
        case 119:// f8
            return KeyEvent.KEYCODE_F8;
        case 120:// f9
            return KeyEvent.KEYCODE_F9;
        case 121:// f10
            return KeyEvent.KEYCODE_F10;
        case 122:// f11
            return KeyEvent.KEYCODE_F11;
        case 123:// f12
            return KeyEvent.KEYCODE_F12;
        case 192:// `
            return KeyEvent.KEYCODE_GRAVE;
        case 49:// 1
            return KeyEvent.KEYCODE_1;
        case 50:// 2
            return KeyEvent.KEYCODE_2;
        case 51:// 3
            return KeyEvent.KEYCODE_3;
        case 52:// 4
            return KeyEvent.KEYCODE_4;
        case 53:// 5
            return KeyEvent.KEYCODE_5;
        case 54:// 6
            return KeyEvent.KEYCODE_6;
        case 55:// 7
            return KeyEvent.KEYCODE_7;
        case 56:// 8
            return KeyEvent.KEYCODE_8;
        case 57:// 9
            return KeyEvent.KEYCODE_9;
        case 48:// 0
            return KeyEvent.KEYCODE_0;
        case 189:// -
            return KeyEvent.KEYCODE_MINUS;
        case 187:// =
            return KeyEvent.KEYCODE_EQUALS;
        case 220:// \
            return KeyEvent.KEYCODE_BACKSLASH;
        case 8:// backspace
            return KeyEvent.KEYCODE_DEL;
        case 9:// tab
            return KeyEvent.KEYCODE_TAB;
        case 81:// Q
            return KeyEvent.KEYCODE_Q;
        case 87:// W
            return KeyEvent.KEYCODE_W;
        case 69:// E
            return KeyEvent.KEYCODE_E;
        case 82:// R
            return KeyEvent.KEYCODE_R;
        case 84:// T
            return KeyEvent.KEYCODE_T;
        case 89:// Y
            return KeyEvent.KEYCODE_Y;
        case 85:// Y
            return KeyEvent.KEYCODE_U;
        case 73:// I
            return KeyEvent.KEYCODE_I;
        case 79:// O
            return KeyEvent.KEYCODE_O;
        case 80:// P
            return KeyEvent.KEYCODE_P;
        case 219:// [
            return KeyEvent.KEYCODE_LEFT_BRACKET;
        case 221:// ]
            return KeyEvent.KEYCODE_RIGHT_BRACKET;
        case 20:// caps lock
            return KeyEvent.KEYCODE_CAPS_LOCK;
        case 65:// A
            return KeyEvent.KEYCODE_A;
        case 83:// S
            return KeyEvent.KEYCODE_S;
        case 68:// D
            return KeyEvent.KEYCODE_D;
        case 70:// F
            return KeyEvent.KEYCODE_F;
        case 71:// G
            return KeyEvent.KEYCODE_G;
        case 72:// H
            return KeyEvent.KEYCODE_H;
        case 74:// J
            return KeyEvent.KEYCODE_J;
        case 75:// K
            return KeyEvent.KEYCODE_K;
        case 76:// L
            return KeyEvent.KEYCODE_L;
        case 186:// ;
            return KeyEvent.KEYCODE_SEMICOLON;
        case 222:// '
            return KeyEvent.KEYCODE_APOSTROPHE;
        case 13:// enter
            return KeyEvent.KEYCODE_ENTER;
        case 16:// shift
            return KeyEvent.KEYCODE_SHIFT_LEFT;
        case 90:// Z
            return KeyEvent.KEYCODE_Z;
        case 88:// X
            return KeyEvent.KEYCODE_X;
        case 67:// C
            return KeyEvent.KEYCODE_C;
        case 86:// V
            return KeyEvent.KEYCODE_V;
        case 66:// B
            return KeyEvent.KEYCODE_B;
        case 78:// N
            return KeyEvent.KEYCODE_N;
        case 77:// M
            return KeyEvent.KEYCODE_M;
        case 188:// ,
            return KeyEvent.KEYCODE_COMMA;
        case 190:// .
            return KeyEvent.KEYCODE_PERIOD;
        case 191:// /
            return KeyEvent.KEYCODE_SLASH;
        case 17:// ctrl
            return KeyEvent.KEYCODE_CTRL_LEFT;
        case 32:// space
            return KeyEvent.KEYCODE_SPACE;
        case 92:// context menu
            return KeyEvent.KEYCODE_MENU;
        case 145:// scroll locl
            return KeyEvent.KEYCODE_SCROLL_LOCK;
        case 19:// break
            return KeyEvent.KEYCODE_BREAK;
        case 45:// insert
            return KeyEvent.KEYCODE_INSERT;
        case 36:// home
            return KeyEvent.KEYCODE_MOVE_HOME;
        case 33:// page up
            return KeyEvent.KEYCODE_PAGE_UP;
        case 46:// delete
            return KeyEvent.KEYCODE_DEL;
        case 35:// end
            return KeyEvent.KEYCODE_MOVE_END;
        case 34:// page down
            return KeyEvent.KEYCODE_PAGE_DOWN;
        case 38:// up
            return KeyEvent.KEYCODE_DPAD_UP;
        case 40:// down
            return KeyEvent.KEYCODE_DPAD_DOWN;
        case 37:// left
            return KeyEvent.KEYCODE_DPAD_LEFT;
        case 39:// right
            return KeyEvent.KEYCODE_DPAD_RIGHT;
        case 93:// right
            return KeyEvent.KEYCODE_MENU;
            // case 10001:// HOME
            // return KeyEvent.KEYCODE_HOME;
        case 10002:// BACK
            return KeyEvent.KEYCODE_BACK;
        default:
            return code;
        }
    }
}
