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

package io.element.android.libraries.pushstore.impl

import androidx.test.platform.app.InstrumentationRegistry
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.concurrent.thread

/**
 * Note: to clear the emulator, invoke:
 * adb uninstall io.element.android.libraries.push.pushstore.impl.test
 */
class DefaultUserPushStoreFactoryTest {
    /**
     * Ensure that creating UserPushStore is thread safe.
     */
    @Test
    fun testParallelCreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val sessionId = SessionId("@alice:server.org")
        val userPushStoreFactory = DefaultUserPushStoreFactory(context, NoOpSessionObserver())
        var userPushStore1: UserPushStore? = null
        val thread1 = thread {
            userPushStore1 = userPushStoreFactory.create(sessionId)
        }
        var userPushStore2: UserPushStore? = null
        val thread2 = thread {
            userPushStore2 = userPushStoreFactory.create(sessionId)
        }
        thread1.join()
        thread2.join()
        runBlocking {
            userPushStore1!!.getNotificationEnabledForDevice().first()
            userPushStore2!!.getNotificationEnabledForDevice().first()
        }
    }
}
