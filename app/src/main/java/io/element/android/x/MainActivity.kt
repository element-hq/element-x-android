package io.element.android.x

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import io.element.android.x.ui.screen.login.LoginActivity
import io.element.android.x.ui.screen.login.RoomListActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Just start the LoginActivity for now.
        // TODO if a session exist, start the room list
        launcher.launch(Intent(this, LoginActivity::class.java))
    }

    private fun startRoomActivityAndFinish() {
        startActivity(Intent(this, RoomListActivity::class.java))
        finish()
    }
}
