/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.di

import dev.zacsweers.metro.Qualifier

/**
 * Qualifies a [File] object which represents the directory holding the cache directories of the sessions.
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Qualifier
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
)
public annotation class SessionsCacheDirectory
