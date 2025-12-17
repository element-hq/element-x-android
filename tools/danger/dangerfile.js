/**
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

const {danger, warn} = require('danger')
const fs = require('fs')
const path = require('path')

/**
 * Note: if you update the checks in this file, please also update the file ./docs/danger.md
 */

// Useful to see what we got in danger object
// warn(JSON.stringify(danger))

const pr = danger.github.pr
const github = danger.github
// User who has created the PR.
const user = pr.user.login
const modified = danger.git.modified_files
const created = danger.git.created_files
const editedFiles = [...modified, ...created]

// Check that the PR has a description
if (pr.body.length == 0) {
    warn("Please provide a description for this PR.")
}

// Warn when there is a big PR
if (editedFiles.length > 50) {
    message("This pull request seems relatively large. Please consider splitting it into multiple smaller ones.")
}

// Request a correct title for each PR
if (pr.title.endsWith("…")) {
    fail("Please provide a complete title that can be used as a changelog entry.")
}

// Request a `PR-` label for each PR
if (pr.labels.filter((label) => label.name.startsWith("PR-")).length != 1) {
    fail("Please add a `PR-` label to categorise the changelog entry.")
}

// check that frozen classes have not been modified
const frozenClasses = [
]

frozenClasses.forEach(frozen => {
    if (editedFiles.some(file => file.endsWith(frozen))) {
        fail("Frozen class `" + frozen + "` has been modified. Please do not modify frozen class.")
    }
  }
)

const previewAnnotations = [
    'androidx.compose.ui.tooling.preview.Preview',
    'io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight',
    'io.element.android.libraries.designsystem.preview.PreviewsDayNight'
]

const filesWithPreviews = editedFiles.filter(file => file.endsWith(".kt")).filter(file => {
    const content = fs.readFileSync(file);
    return previewAnnotations.some((ann) => content.includes("import " + ann));
})

const composablePreviewProviderContents = fs.readFileSync('tests/uitests/src/test/kotlin/base/ComposablePreviewProvider.kt');
const packageTreesRegex = /private val PACKAGE_TREES = arrayOf\(([\w\W]+?)\n\)/gm;
const packageTreesMatch = packageTreesRegex.exec(composablePreviewProviderContents)[1];
const scannedPreviewPackageTrees = packageTreesMatch
    .replaceAll("\"", "")
    .replaceAll(",", "")
    .split('\n').map((line) => line.trim())
    .filter((line) => line.length > 0);

const previewPackagesNotIncludedInScreenshotTests = filesWithPreviews.map((file) => {
    const content = fs.readFileSync(file);
    const packageRegex = /package\s+([a-zA-Z0-9.]+)/;
    const packageMatch = packageRegex.exec(content);

    if (!packageMatch || packageMatch.length != 2) {
        return null;
    }

    return packageMatch[1];


}).filter((package) => {
    if (!package) {
        return false;
    }
    if (!scannedPreviewPackageTrees.some((prefix) => package.includes(prefix))) {
        return true;
    }
});

if (previewPackagesNotIncludedInScreenshotTests.length > 0) {
    const packagesList = previewPackagesNotIncludedInScreenshotTests.map((p) => '- `' + p + '`').join("\n");
    warn("You have made changes to a file containing a `@Preview` annotated function but its package name prefix is not included in the `ComposablePreviewProvider`.\nPackages missing in `tests/uitests/src/test/kotlin/base/ComposablePreviewProvider.kt`: \n" + packagesList);
}

// Check for pngs on resources
const hasPngs = editedFiles.filter(file => {
    file.toLowerCase().endsWith(".png") && !file.includes("snapshots/images/") // Exclude screenshots
}).length > 0
if (hasPngs) {
    warn("You seem to have made changes to some images. Please consider using an vector drawable.")
}

// Check that translations have not been modified by developers
const translationAllowList = [
    "ElementBot",
]

if (!translationAllowList.includes(user)) {
   if (editedFiles.some(file => file.endsWith("translations.xml"))) {
       fail("Some translation files have been edited. Only user `ElementBot` (i.e. translations coming from Localazy) is allowed to do that.\nPlease read more about translations management [in the doc](https://github.com/element-hq/element-x-android/blob/develop/CONTRIBUTING.md#strings).")
   }
}
