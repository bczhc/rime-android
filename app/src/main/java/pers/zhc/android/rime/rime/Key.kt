package pers.zhc.android.rime.rime

data class KeyEvent(
    val keyCode: Int,
    val modifier: Int,
)

enum class KeyStatus {
    ACCEPTED,
    PASS,
}
