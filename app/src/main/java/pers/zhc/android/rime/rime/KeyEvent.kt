package pers.zhc.android.rime.rime

import android.view.KeyEvent
import android.view.KeyEvent.*
import pers.zhc.android.rime.rime.KeyEvent as RimeKey

data class KeyEvent(
    val keyCode: Int,
    val modifier: Int,
)

enum class KeyStatus {
    ACCEPTED,
    PASS,
}

// value: keyCode without and with shift modified
val rimeKeyCodeMap by lazy {
    val map1 = arrayOf(
        XK_parenright,
        XK_exclam,
        XK_at,
        XK_numbersign,
        XK_dollar,
        XK_percent,
        XK_asciicircum,
        XK_ampersand,
        XK_asterisk,
        XK_parenleft,
    )

    buildMap {
        put(KEYCODE_GRAVE, Pair(XK_grave, XK_asciitilde))
        put(KEYCODE_MINUS, Pair(XK_minus, XK_underscore))
        put(KEYCODE_EQUALS, Pair(XK_equal, XK_plus))
        (KEYCODE_0..KEYCODE_9).forEach {
            put(it, Pair(it - KEYCODE_0 + XK_0, map1[it - KEYCODE_0]))
        }
        (KEYCODE_A..KEYCODE_Z).forEach {
            put(it, Pair(it - KEYCODE_A + XK_a, it - KEYCODE_A + XK_A))
        }
        put(KEYCODE_LEFT_BRACKET, Pair(XK_bracketleft, XK_braceleft))
        put(KEYCODE_RIGHT_BRACKET, Pair(XK_bracketright, XK_braceright))
        put(KEYCODE_BACKSLASH, Pair(XK_backslash, XK_bar))
        put(KEYCODE_SEMICOLON, Pair(XK_semicolon, XK_colon))
        put(KEYCODE_APOSTROPHE, Pair(XK_apostrophe, XK_quotedbl))
        put(KEYCODE_COMMA, Pair(XK_comma, XK_less))
        put(KEYCODE_PERIOD, Pair(XK_period, XK_greater))
        put(KEYCODE_SLASH, Pair(XK_slash, XK_question))
    }
}

fun toRimeKey(event: KeyEvent): RimeKey {
    val release = event.action == ACTION_UP
    var modifier = kEmpty

    if (release) modifier = modifier or kReleaseMask
    if (event.isShiftPressed) modifier = modifier or kShiftMask
    if (event.isAltPressed) modifier = modifier or kAltMask
    if (event.isCtrlPressed) modifier = modifier or kControlMask
    if (event.isMetaPressed) modifier = modifier or kSuperMask

    val code = when (val c = event.keyCode) {
        KEYCODE_SPACE -> XK_space
        KEYCODE_ENTER -> XK_Return
        KEYCODE_BACK -> XK_BackSpace
        KEYCODE_TAB -> XK_Tab
        KEYCODE_CAPS_LOCK -> XK_Caps_Lock
        KEYCODE_ESCAPE -> XK_Escape
        KEYCODE_CTRL_LEFT -> XK_Control_L
        KEYCODE_CTRL_RIGHT -> XK_Control_R
        KEYCODE_ALT_LEFT -> XK_Alt_L
        KEYCODE_ALT_RIGHT -> XK_Alt_R
        KEYCODE_SHIFT_LEFT -> XK_Shift_L
        KEYCODE_SHIFT_RIGHT -> XK_Shift_R
        KEYCODE_META_LEFT -> XK_Super_L
        KEYCODE_META_RIGHT -> XK_Super_R
        else -> {
            val mapped = rimeKeyCodeMap[c]!!
            if (event.isShiftPressed) {
                mapped.first
            } else {
                mapped.second
            }
        }
    }
    return RimeKey(code, modifier)
}
