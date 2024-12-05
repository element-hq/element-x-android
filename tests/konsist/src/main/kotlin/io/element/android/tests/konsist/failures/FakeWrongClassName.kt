/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist.failures

// Make test `Fake classes must be named using Fake and the interface it fakes` fails

interface MyInterface

// This class should be named FakeMyInterface
class FakeWrongClassName : MyInterface

class MyClass {
    interface MyFactory
}

// This class should be named FakeMyClassMyFactory
class FakeWrongClassSubInterfaceName : MyClass.MyFactory
