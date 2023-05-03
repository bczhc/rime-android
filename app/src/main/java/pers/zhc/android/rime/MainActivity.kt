package pers.zhc.android.rime

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pers.zhc.android.rime.jni.Rime

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val version = Rime.getRimeVersion()
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show()
    }
}
