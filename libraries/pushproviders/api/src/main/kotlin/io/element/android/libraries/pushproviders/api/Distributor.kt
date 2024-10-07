/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

/**
 * Firebase does not have the concept of distributor. So for Firebase, there will be one distributor:
 * Distributor("Firebase", "Firebase").
 *
 * For UnifiedPush, for instance, the Distributor can be:
 * Distributor("io.heckel.ntfy", "ntfy").
 * But other values are possible.
 */
data class Distributor(
    val value: String,
    val name: String,
)
