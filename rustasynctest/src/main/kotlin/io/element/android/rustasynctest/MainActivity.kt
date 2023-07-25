/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.rustasynctest

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.sdkTestAsync
import org.matrix.rustcomponents.sdk.setupTracing

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupTracing("")

        findViewById<Button>(R.id.button).setOnClickListener {
            val firstJob = callRustAsyncFun(0)
//            firstJob.cancel()

            lifecycleScope.launch {
                delay(1000)
                firstJob.cancel()

                for (i in 0 until 5) {
                    callRustAsyncFun(i + 1)
                }
            }
        }
    }

    private fun callRustAsyncFun(message: Int) = lifecycleScope.launch(Dispatchers.Default) {
        coroutineScope {  }
        if (!isActive) {
            println("It was canceled before starting!")
        } else {
            println("Started getting message $message")
        }
        val result = sdkTestAsync(message.toUInt(), 5.toULong())
        if (!isActive) {
            println("It was canceled!")
        } else {
            withContext(Dispatchers.Main) {
//                Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
                println(result)
            }
        }
    }
}
