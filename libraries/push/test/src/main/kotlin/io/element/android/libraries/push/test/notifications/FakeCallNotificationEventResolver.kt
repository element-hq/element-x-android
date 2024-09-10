/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.push.impl.notifications.CallNotificationEventResolver
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent

class FakeCallNotificationEventResolver(
    var resolveEventLambda: (sessionId: SessionId, notificationData: NotificationData, forceNotify: Boolean) -> NotifiableEvent? = { _, _, _ -> null },
) : CallNotificationEventResolver {
    override fun resolveEvent(sessionId: SessionId, notificationData: NotificationData, forceNotify: Boolean): NotifiableEvent? {
        return resolveEventLambda(sessionId, notificationData, forceNotify)
    }
}
