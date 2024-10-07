/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesTo
import io.element.android.libraries.di.AppScope

@ContributesTo(AppScope::class)
interface VectorUnifiedPushMessagingReceiverBindings {
    fun inject(receiver: VectorUnifiedPushMessagingReceiver)
}
