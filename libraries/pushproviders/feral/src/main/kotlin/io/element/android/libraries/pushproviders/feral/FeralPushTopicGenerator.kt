/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.FeralPushConfig
import java.security.SecureRandom

/** Creates unguessable ntfy topic names: [FeralPushConfig.TOPIC_PREFIX] + random lowercase hex. */
fun interface FeralPushTopicGenerator {
    fun generate(): String
}

@ContributesBinding(AppScope::class)
class DefaultFeralPushTopicGenerator : FeralPushTopicGenerator {
    private val random = SecureRandom()

    override fun generate(): String {
        val bytes = ByteArray(FeralPushConfig.TOPIC_RANDOM_BYTES).also(random::nextBytes)
        return FeralPushConfig.TOPIC_PREFIX + bytes.joinToString("") { "%02x".format(it) }
    }
}
