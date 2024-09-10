/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.tests.testutils.lambda.lambdaError

class FakeOnNotifiableEventReceived(
    private val onNotifiableEventReceivedResult: (NotifiableEvent) -> Unit = { lambdaError() },
) : OnNotifiableEventReceived {
    override fun onNotifiableEventReceived(notifiableEvent: NotifiableEvent) {
        onNotifiableEventReceivedResult(notifiableEvent)
    }
}
