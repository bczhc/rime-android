package pers.zhc.android.rime.util

import android.content.Context
import android.os.Handler
import android.os.Looper

fun Context.runOnUiThread(task: () -> Unit) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        val handler = Handler(Looper.getMainLooper())
        handler.post { task() }
    } else {
        task()
    }
}
