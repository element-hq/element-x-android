/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.tests.testutils.lambda.lambdaError

class FakeOnNotifiableEventReceived(
    private val onNotifiableEventsReceivedResult: (List<NotifiableEvent>) -> Unit = { lambdaError() },
) : OnNotifiableEventReceived {
    override fun onNotifiableEventsReceived(notifiableEvents: List<NotifiableEvent>) {
        onNotifiableEventsReceivedResult(notifiableEvents)
    }
}
