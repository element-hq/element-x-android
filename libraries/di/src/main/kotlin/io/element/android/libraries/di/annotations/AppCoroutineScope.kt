/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.di.annotations

import javax.inject.Qualifier

/**
 * Qualifies a [CoroutineScope] object which represents the base coroutine scope to use for the application.
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Qualifier
annotation class AppCoroutineScope
