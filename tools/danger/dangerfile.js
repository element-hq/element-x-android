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

// Request a changelog for each PR
const changelogAllowList = [
    "dependabot[bot]",
]

const requiresChangelog = !changelogAllowList.includes(user)

if (requiresChangelog) {
    const changelogFiles = editedFiles.filter(file => file.startsWith("changelog.d/"))

    if (changelogFiles.length == 0) {
        warn("Please add a changelog. See instructions [here](https://github.com/vector-im/element-android/blob/develop/CONTRIBUTING.md#changelog)")
    } else {
        const validTowncrierExtensions = [
            "bugfix",
            "doc",
            "feature",
            "misc",
            "wip",
        ]
        if (!changelogFiles.every(file => validTowncrierExtensions.includes(file.split(".").pop()))) {
            fail("Invalid extension for changelog. See instructions [here](https://github.com/vector-im/element-android/blob/develop/CONTRIBUTING.md#changelog)")
        }
    }
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
//      org: "vector-im",
//      username: user,
//   }).then((result) => {
    github.api.rest.teams.getMembershipForUserInOrg({
        org: "vector-im",
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
    'io.element.android.libraries.designsystem.preview.LargeHeightPreview',
    'io.element.android.libraries.designsystem.preview.ElementPreviews'
]

const filesWithPreviews = editedFiles.filter(file => file.endsWith(".kt")).filter(file => {
    const content = fs.readFileSync(file);
    return previewAnnotations.some((ann) => content.includes(ann));
})

const buildFilesWithMissingProcessor = filesWithPreviews.map(file => {
    let parent = path.dirname(file);
    while (fs.statSync(path.join(parent, 'build.gradle.kts'), {throwIfNoEntry: false}) === undefined) {
        parent = path.dirname(parent);
    }
    return path.join(parent, 'build.gradle.kts');
}).filter((value, index, array) => array.indexOf(value) === index).filter(buildFile => {
    const content = fs.readFileSync(buildFile);
    return !content.includes('ksp(libs.showkase.processor)');
})

if (buildFilesWithMissingProcessor.length > 0) {
    warn("You have made changes to a file containing a `@Preview` annotated function but its module doesn't include the showkase processor. Missing processor in: " + buildFilesWithMissingProcessor.join(", "))
}

// Check for screenshots on view changes
const hasChangedViews = filesWithPreviews.length > 0
if (hasChangedViews) {
    if (!pr.body.includes("user-images")) {
        warn("You seem to have made changes to views. Please consider adding screenshots.")
    }
}

// Check for pngs on resources
const hasPngs = editedFiles.filter(file => {
    file.toLowerCase().endsWith(".png") && !file.includes("snapshots/images/") // Exclude screenshots
}).length > 0
if (hasPngs) {
    warn("You seem to have made changes to some images. Please consider using an vector drawable.")
}

// Check for reviewers
if (github.requested_reviewers.users.length == 0 && !pr.draft) {
    warn("Please add a reviewer to your PR.")
}

// Check that translations have not been modified by developers
const translationAllowList = [
    "RiotTranslateBot",
    "github-actions[bot]",
]

if (!translationAllowList.includes(user)) {
   if (editedFiles.some(file => file.endsWith("strings.xml") && !file.endsWith("values/strings.xml"))) {
       fail("Some translation files have been edited. Only user `RiotTranslateBot` (i.e. translations coming from Weblate) or `github-actions[bot]` (i.e. translations coming from automation) are allowed to do that.\nPlease read more about translations management [in the doc](https://github.com/vector-im/element-android/blob/develop/CONTRIBUTING.md#internationalisation).")
   }

   // Check that new strings are not added to `values/strings.xml`
   if (editedFiles.some(file => file.endsWith("ui-strings/src/main/res/values/strings.xml"))) {
      fail("`ui-strings/src/main/res/values/strings.xml` has been edited. This file will be overridden in the next strings synchronisation. Please add new strings in the file `values/strings_eax.xml` instead.")
   }
}
