/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.di

/**
 * A [DaggerComponentOwner] is anything that "owns" a Dagger Component.
 *
 */
interface DaggerComponentOwner {
    /** This is either a component, or a list of components. */
    val daggerComponent: Any
}
