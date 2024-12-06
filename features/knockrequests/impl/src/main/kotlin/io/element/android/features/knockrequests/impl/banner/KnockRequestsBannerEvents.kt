/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import io.element.android.features.knockrequests.impl.KnockRequest

sealed interface KnockRequestsBannerEvents {
    data class Accept(val knockRequest: KnockRequest) : KnockRequestsBannerEvents
    data object Dismiss : KnockRequestsBannerEvents
}
