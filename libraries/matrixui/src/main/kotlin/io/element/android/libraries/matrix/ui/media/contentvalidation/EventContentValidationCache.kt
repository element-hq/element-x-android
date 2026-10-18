/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media.contentvalidation

import io.element.android.libraries.matrix.api.core.EventId

/**
 * A cache that holds the validation state of event contents, based on their [EventId].
 */
interface EventContentValidationCache {
    /**
     * Returns the [ContentValidationState] for the given [eventId]. If none exists, it creates a new one with [ContentValidationValue.Unknown].
     */
    operator fun get(eventId: EventId): ContentValidationState
}
