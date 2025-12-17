/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
