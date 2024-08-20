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

// Check for a sign-off
const signOff = "Signed-off-by:"

// Please add new names following the alphabetical order.
const allowList = [
    "aringenbach",
    "BillCarsonFr",
    "bmarty",
    "csmith",
    "dependabot[bot]",
    "Florian14",
    "ganfra",
    "github-actions[bot]",
    "jmartinesp",
    "jonnyandrew",
    "julioromano",
    "kittykat",
    "langleyd",
    "MadLittleMods",
    "manuroe",
    "renovate[bot]",
    "stefanceriu",
    "yostyle",
]

function signoff_needed(reason) {
    message("Sign-off required, " + reason)
    const hasPRBodySignOff = pr.body.includes(signOff)
    const hasCommitSignOff = danger.git.commits.every(commit => commit.message.includes(signOff))
    if (!hasPRBodySignOff && !hasCommitSignOff) {
        fail("Please add a sign-off to either the PR description or to the commits themselves. See instructions [here](https://matrix-org.github.io/synapse/latest/development/contributing_guide.html#sign-off).")
    }
}

function signoff_unneeded(reason) {
    message("Sign-off not required, " + reason)
}

// Somewhat awkward phrasing, dangerfile is not in an async context.
if (allowList.includes(user)) {
    signoff_unneeded("allow-list")
} else {
//  github.api.rest.orgs.checkMembershipForUser({
//      org: "element-hq",
//      username: user,
//   }).then((result) => {
    github.api.rest.teams.getMembershipForUserInOrg({
        org: "element-hq",
        team_slug: "vector-core",
        username: user,
    }).then((result) => {
        if (result.status == 204 || result.status == 200) {
            signoff_unneeded("team-member")
        }
        else {
            signoff_needed("not-team-member")
        }
    }).catch((error) => { 
        if (error.response.status == 404) {
            signoff_needed("not-team-member");
        } else {
            console.log(error); signoff_needed("error") 
        }
    })
}

const previewAnnotations = [
    'androidx.compose.ui.tooling.preview.Preview',
    'io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight',
    'io.element.android.libraries.designsystem.preview.PreviewsDayNight'
]

const filesWithPreviews = editedFiles.filter(file => file.endsWith(".kt")).filter(file => {
    const content = fs.readFileSync(file);
    return previewAnnotations.some((ann) => content.includes(ann));
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
    "RiotTranslateBot",
    "github-actions[bot]",
]

if (!translationAllowList.includes(user)) {
   if (editedFiles.some(file => file.endsWith("strings.xml") && !file.endsWith("values/strings.xml"))) {
       fail("Some translation files have been edited. Only user `RiotTranslateBot` (i.e. translations coming from Weblate) or `github-actions[bot]` (i.e. translations coming from automation) are allowed to do that.\nPlease read more about translations management [in the doc](https://github.com/element-hq/element-android/blob/develop/CONTRIBUTING.md#internationalisation).")
   }

   // Check that new strings are not added to `values/strings.xml`
   if (editedFiles.some(file => file.endsWith("ui-strings/src/main/res/values/strings.xml"))) {
      fail("`ui-strings/src/main/res/values/strings.xml` has been edited. This file will be overridden in the next strings synchronisation. Please add new strings in the file `values/strings_eax.xml` instead.")
   }
}
