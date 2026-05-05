/*
 * Copyright 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList

/**
 * Per-app locale switcher backed by platform LocaleManager (API 33+).
 *
 * Element X intentionally avoids AppCompat, so we use the platform API directly
 * and no-op on API < 33; on those devices the app follows the system locale.
 */
internal object AppLocale {
    /**
     * BCP-47 tag of the locales hard-coded in the picker. The first entry's tag
     * is `null`, meaning "follow system locale" (clears the per-app override).
     */
    val choices: List<Choice> = listOf(
        Choice(tag = null, displayName = "System default"),
        Choice(tag = "en", displayName = "English"),
        Choice(tag = "zh-CN", displayName = "简体中文"),
        Choice(tag = "zh-TW", displayName = "繁體中文"),
    )

    data class Choice(val tag: String?, val displayName: String)

    fun current(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        val list = context.getSystemService(LocaleManager::class.java)?.applicationLocales
        if (list == null || list.isEmpty) return null
        return list[0]?.toLanguageTag()
    }

    fun set(context: Context, tag: String?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val manager = context.getSystemService(LocaleManager::class.java) ?: return
        manager.applicationLocales = if (tag.isNullOrBlank()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(tag)
        }
    }
}
