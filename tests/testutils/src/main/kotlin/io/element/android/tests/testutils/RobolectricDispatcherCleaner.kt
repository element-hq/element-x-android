/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.testutils

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import org.junit.Assert.assertFalse
import org.junit.rules.TestRule
import kotlin.coroutines.CoroutineContext

object RobolectricDispatcherCleaner {
    // HACK: Workaround for https://github.com/robolectric/robolectric/issues/7055#issuecomment-1551119229
    fun clearAndroidUiDispatcher(pkg: String = "androidx.compose.ui.platform") {
        val clazz = javaClass.classLoader!!.loadClass("$pkg.AndroidUiDispatcher")
        val combinedContextClass = javaClass.classLoader!!.loadClass("kotlin.coroutines.CombinedContext")
        val companionClazz = clazz.getDeclaredField("Companion").get(clazz)
        val combinedContext = companionClazz.javaClass.getDeclaredMethod("getMain")
            .invoke(companionClazz) as CoroutineContext
        val androidUiDispatcher = combinedContextClass.getDeclaredField("element")
            .apply { isAccessible = true }
            .get(combinedContext)
            .let { clazz.cast(it) }
        var scheduledFrameDispatch = clazz.getDeclaredField("scheduledFrameDispatch")
            .apply { isAccessible = true }
            .getBoolean(androidUiDispatcher)
        var scheduledTrampolineDispatch = clazz.getDeclaredField("scheduledTrampolineDispatch")
            .apply { isAccessible = true }
            .getBoolean(androidUiDispatcher)
        val dispatchCallback = clazz.getDeclaredField("dispatchCallback")
            .apply { isAccessible = true }
            .get(androidUiDispatcher) as Runnable
        if (scheduledFrameDispatch || scheduledTrampolineDispatch) {
            dispatchCallback.run()
            scheduledFrameDispatch = clazz.getDeclaredField("scheduledFrameDispatch")
                .apply { isAccessible = true }
                .getBoolean(androidUiDispatcher)
            scheduledTrampolineDispatch = clazz.getDeclaredField("scheduledTrampolineDispatch")
                .apply { isAccessible = true }
                .getBoolean(androidUiDispatcher)
        }
        assertFalse(scheduledFrameDispatch)
        assertFalse(scheduledTrampolineDispatch)
    }
}

fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.setSafeContent(content: @Composable () -> Unit) {
    RobolectricDispatcherCleaner.clearAndroidUiDispatcher()
    setContent(content)
}
