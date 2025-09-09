/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.di

/**
 * A [DependencyInjectionGraphOwner] is anything that "owns" a DI Graph.
 *
 */
interface DependencyInjectionGraphOwner {
    /** This is either a graph, or a list of graphs. */
    val graph: Any
}
