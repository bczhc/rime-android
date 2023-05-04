package pers.zhc.android.rime.jni

data class KeyEvent(
    val keyCode: Int,
    val modifier: Int,
)

enum class KeyStatus {
    ACCEPTED,
    PASS,
}
