/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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

    override fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        for (event in notifiableEvents) {
            onNotifiableEventReceived(event)
        }
    }
}
