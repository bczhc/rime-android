package pers.zhc.android.rime

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.gson.Gson

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CONTEXT = this
    }

    companion object {
        val GSON by lazy { Gson() }
        @SuppressLint("StaticFieldLeak")
        lateinit var CONTEXT: Context
    }
}
