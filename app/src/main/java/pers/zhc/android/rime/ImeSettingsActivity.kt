package pers.zhc.android.rime

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pers.zhc.android.rime.databinding.ImeSettingsActivityBinding
import pers.zhc.android.rime.rime.Engine
import pers.zhc.android.rime.rime.JNI
import pers.zhc.android.rime.rime.KeyEvent
import pers.zhc.android.rime.util.ToastUtils
import kotlin.concurrent.thread

class ImeSettingsActivity: AppCompatActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ImeSettingsActivityBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        ToastUtils.show(this, JNI.getRimeVersion())

        bindings.testButton.setOnClickListener {
            val userDataDir = bindings.userDataDirEt.text.toString()
            val sharedDataDir = bindings.sharedDataDirEt.text.toString().ifEmpty { null }
            val engine = Engine.create(userDataDir, sharedDataDir)
            ToastUtils.show(this, "Deploying...")
            thread {
                val result = engine.waitForDeployment()
                if (!result) {
                    ToastUtils.show(this, "Deployment failed")
                } else {
                    ToastUtils.show(this, "Done")
                }

                val session = engine.createSession()
                val keyStatus = session.processKey(KeyEvent(103 /* g */, 0))
                println(keyStatus)

                val context = session.getContext()!!
                println(context)
                println(context.getCandidates())
                println(session.getCommit())
            }
        }
    }
}
