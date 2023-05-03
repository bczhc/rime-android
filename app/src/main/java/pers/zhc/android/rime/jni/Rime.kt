package pers.zhc.android.rime.jni

class Rime {
    companion object {
        init {
            System.loadLibrary("rime")
            System.loadLibrary("rime_jni")
        }

        @JvmStatic
        external fun getRimeVersion(): String
    }
}
