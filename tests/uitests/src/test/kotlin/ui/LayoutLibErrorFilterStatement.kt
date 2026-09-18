/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package ui

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Workaround for Paparazzi 2.0.0-alpha05 / LayoutLib 16.2.1 bug where
 * HandlerThread_Delegate calls Thread.setPosixNicenessInternal which doesn't exist on JVM.
 * Tracked in https://github.com/cashapp/paparazzi/issues/2342 — fixed in layoutlib 16.2.3.
 * Remove this workaround once Paparazzi ships with layoutlib >= 16.2.3.
 *
 * Paparazzi's PaparazziLogger collects background thread errors and re-throws them via
 * assertNoErrors(). This Statement wrapper catches the known NoSuchMethodError so the test
 * can pass despite the LayoutLib bug.
 *
 * Note: forcing layoutlib >= 16.2.3 ourselves does not work, and this is not just a matter of
 * declaring the version. The `setThreadNiceness` fix and the removal of `Bridge.prepareThread()`,
 * which Paparazzi 2.0.0-alpha05 still calls, both landed in 16.2.3, so every screenshot test fails
 * with `NoSuchMethodError: Bridge.prepareThread()`. 16.2.2 is the last compatible release and it does
 * not have the fix. We have to wait for a Paparazzi release.
 *
 * Swallowing the error is not harmless: HandlerThread.run() publishes its Looper and then dies
 * before Looper.loop(), on a background thread racing the render, so a screenshot can be recorded
 * from a partially rendered frame. That is why previews with animations or a bottom sheet flip
 * between two results (sometimes an entirely blank image) on every recording.
 */
class LayoutLibErrorFilterStatement : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    base.evaluate()
                } catch (e: NoSuchMethodError) {
                    if (e.message?.contains("setPosixNicenessInternal") != true) throw e
                } catch (npe: NullPointerException) {
                    // Also catch this error on `HorizontalFloatingToolbarPreview` and `HorizontalFloatingToolbarNoFabPreview`
                    if (npe.message?.contains("""Cannot invoke "java.util.ArrayList.size()" because "childrenList" is null""") != true) throw npe
                }
            }
        }
    }
}
