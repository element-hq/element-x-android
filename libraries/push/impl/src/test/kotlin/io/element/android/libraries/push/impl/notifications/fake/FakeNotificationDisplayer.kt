/*
 * Copyright (c) 2021 New Vector Ltd
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

package io.element.android.libraries.push.impl.notifications.fake

import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.NotificationDisplayer
import io.element.android.libraries.push.impl.notifications.NotificationIdProvider
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

class FakeNotificationDisplayer {
    val instance = mockk<NotificationDisplayer>(relaxed = true)

    fun givenDisplayDiagnosticNotificationResult(result: Boolean) {
        every { instance.displayDiagnosticNotification(any()) } returns result
    }

    fun verifySummaryCancelled() {
        verify { instance.cancelNotificationMessage(tag = null, NotificationIdProvider().getSummaryNotificationId(A_SESSION_ID)) }
    }

    fun verifyNoOtherInteractions() {
        confirmVerified(instance)
    }

    fun verifyInOrder(verifyBlock: NotificationDisplayer.() -> Unit) {
        verifyOrder { verifyBlock(instance) }
        verifyNoOtherInteractions()
    }
}
