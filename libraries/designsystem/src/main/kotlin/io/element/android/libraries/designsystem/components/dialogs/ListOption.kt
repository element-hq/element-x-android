/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.dialogs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Used to store the visual data for a list option.
 */
data class ListOption(
    val title: String,
    val subtitle: String? = null,
)

/** Creates an immutable list of [ListOption]s from the given [values], using them as titles. */
fun listOptionOf(vararg values: String): ImmutableList<ListOption> {
    return values.map { ListOption(it) }.toImmutableList()
}
