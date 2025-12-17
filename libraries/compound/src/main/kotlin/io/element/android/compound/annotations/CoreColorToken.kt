/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.annotations

@RequiresOptIn(
    message = "This is a Core color token, which should only be used to declare semantic colors, otherwise it" +
        " would look the same on both light and dark modes. Only use it as is if you know what you are doing."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class CoreColorToken
