/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
