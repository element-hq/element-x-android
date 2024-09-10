/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

/**
 * Some data used to configure the creation of timeline items.
 * @param computeReadReceipts when false, read receipts will be empty.
 * @param computeReactions when false, reactions will be empty.
 */
data class TimelineItemsFactoryConfig(
    val computeReadReceipts: Boolean,
    val computeReactions: Boolean,
)
