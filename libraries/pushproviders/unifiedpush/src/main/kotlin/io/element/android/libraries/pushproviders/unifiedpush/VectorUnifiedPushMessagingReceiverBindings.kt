/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import org.unifiedpush.android.connector.MessagingReceiver

@ContributesTo(AppScope::class)
interface VectorUnifiedPushMessagingReceiverBindings {
    fun inject(receiver: VectorUnifiedPushMessagingReceiver)

    @Binds
    fun bindsMessagingReceiver(vectorUnifiedPushMessagingReceiver: VectorUnifiedPushMessagingReceiver): MessagingReceiver
}
