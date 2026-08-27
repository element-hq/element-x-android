/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

/**
 * Observes the rooms which should be exposed as Android direct share targets and
 * keeps the published shortcuts up to date for the lifetime of the session.
 *
 * This owns its own room list so that the shortcuts are not affected by the
 * filters the user applies to the room list UI.
 */
interface DirectShareShortcutsObserver {
    /**
     * Start observing rooms and publishing shortcuts.
     * Calling this while already started has no effect.
     */
    fun start()
}
