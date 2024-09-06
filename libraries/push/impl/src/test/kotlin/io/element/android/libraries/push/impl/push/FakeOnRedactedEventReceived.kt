/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.tests.testutils.lambda.lambdaError

class FakeOnRedactedEventReceived(
    private val onRedactedEventReceivedResult: (ResolvedPushEvent.Redaction) -> Unit = { lambdaError() },
) : OnRedactedEventReceived {
    override fun onRedactedEventReceived(redaction: ResolvedPushEvent.Redaction) {
        onRedactedEventReceivedResult(redaction)
    }
}
