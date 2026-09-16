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
