package io.element.android.x.initializer

import android.content.Context
import androidx.startup.Initializer
import com.airbnb.mvrx.Mavericks

class MavericksInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Mavericks.initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf()


}