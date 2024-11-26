/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

// TODO add your ui models. Remove the eventSink if you don't have events.
// Do not use default value, so no member get forgotten in the presenters.
data class KnockRequestsListState(
    val eventSink: (KnockRequestsListEvents) -> Unit
)
