/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.di.annotations

import javax.inject.Qualifier

/**
 * Qualifies a [CoroutineScope] object which represents the base coroutine scope to use for an active session.
 */
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Qualifier
annotation class SessionCoroutineScope
