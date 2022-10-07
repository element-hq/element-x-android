package io.element.android.x

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import io.element.android.x.ui.screen.login.LoginActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Just start the LoginActivity for now.
        // TODO if a session exist, start the room list
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
