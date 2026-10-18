/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.system

import android.content.Context
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.ApplicationContext

private const val VULKAN_VERSION_1_0 = 0x400003

/**
 * Checks if the device supports Vulkan 1.0.
 *
 * This is needed for the location screens that contain maps using MapLibre UI components.
 *
 * Needed until https://github.com/maplibre/maplibre-native/issues/3079 is resolved and we can automatically choose between OpenGL and Vulkan renderers,
 * or no devices support OpenGL anymore.
 */
@Inject
class DeviceHasVulkanSupport(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.vulkan.version", VULKAN_VERSION_1_0)
    }
}
