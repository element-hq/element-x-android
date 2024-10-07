/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.sessionstorage.test.observer

import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver

class NoOpSessionObserver : SessionObserver {
    override fun addListener(listener: SessionListener) = Unit
    override fun removeListener(listener: SessionListener) = Unit
}
