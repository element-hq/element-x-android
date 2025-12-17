/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl

import androidx.test.platform.app.InstrumentationRegistry
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.api.UserPushStore
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
        val userPushStoreFactory = DefaultUserPushStoreFactory(context)
        var userPushStore1: UserPushStore? = null
        val thread1 = thread {
            userPushStore1 = userPushStoreFactory.getOrCreate(sessionId)
        }
        var userPushStore2: UserPushStore? = null
        val thread2 = thread {
            userPushStore2 = userPushStoreFactory.getOrCreate(sessionId)
        }
        thread1.join()
        thread2.join()
        runBlocking {
            userPushStore1!!.getNotificationEnabledForDevice().first()
            userPushStore2!!.getNotificationEnabledForDevice().first()
        }
    }
}
