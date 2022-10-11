package io.element.android.x

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import io.element.android.x.ui.screen.login.LoginActivity
import io.element.android.x.ui.screen.roomlist.RoomListActivity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // Launch the room Activity and finish
                startRoomActivityAndFinish()
            } else {
                finish()
            }
        }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            if (viewModel.hasSession()) {
                startRoomActivityAndFinish()
            } else {
                launcher.launch(Intent(this@MainActivity, LoginActivity::class.java))
            }
        }
    }

    private fun startRoomActivityAndFinish() {
        startActivity(Intent(this, RoomListActivity::class.java))
        finish()
    }
}
