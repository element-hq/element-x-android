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

import android.app.Notification
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.NotificationDisplayer
import io.element.android.tests.testutils.lambda.LambdaNoParamRecorder
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.LambdaThreeParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaTwoParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value

class FakeNotificationDisplayer(
    var showNotificationMessageResult: LambdaThreeParamsRecorder<String?, Int, Notification, Boolean> = lambdaRecorder { _, _, _ -> true },
    var cancelNotificationMessageResult: LambdaTwoParamsRecorder<String?, Int, Unit> = lambdaRecorder { _, _ -> },
    var displayDiagnosticNotificationResult: LambdaOneParamRecorder<Notification, Boolean> = lambdaRecorder { _ -> true },
    var dismissDiagnosticNotificationResult: LambdaNoParamRecorder<Unit> = lambdaRecorder { -> },
) : NotificationDisplayer {
    override fun showNotificationMessage(tag: String?, id: Int, notification: Notification): Boolean {
        return showNotificationMessageResult(tag, id, notification)
    }

    override fun cancelNotificationMessage(tag: String?, id: Int) {
        return cancelNotificationMessageResult(tag, id)
    }

    override fun displayDiagnosticNotification(notification: Notification): Boolean {
        return displayDiagnosticNotificationResult(notification)
    }

    override fun dismissDiagnosticNotification() {
        return dismissDiagnosticNotificationResult()
    }

    fun verifySummaryCancelled(times: Int = 1) {
        cancelNotificationMessageResult.assertions().isCalledExactly(times).withSequence(
            listOf(value(null), value(NotificationIdProvider.getSummaryNotificationId(A_SESSION_ID)))
        )
    }
}
