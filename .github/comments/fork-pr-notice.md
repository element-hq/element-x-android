Thank you for your contribution! Here are a few things to check in the PR to ensure it's reviewed as quickly as possible:

- If your pull request adds a feature or modifies the UI, this must be based on a feature request already discussed in
  [element-meta](https://github.com/element-hq/element-meta/issues), and previously discussed with and agreed on with our product and design teams
  (with the `X-Accepting-PRs` or `X-Accepting-PoC-PRs` label), otherwise it will be closed until it's been discussed with our product and
  design teams. Also, any Android pull request adding or modifying a feature should have an equivalent one in the
  [Element X iOS repo](https://github.com/element-hq/element-x-ios) unless it only affects an Android-only behaviour or is behind a disabled
  feature flag, since we need parity in both clients to consider a feature done.
- Your branch should be based on `origin/develop`, at least when it was created.
- The title of the PR will be used for release notes, so it needs to describe the change visible to the user.
- The tests pass locally running `./gradlew test`.
- The code quality check suite pass locally running `./gradlew runQualityChecks`.
- If you modified anything related to the UI, including previews, you'll have to record new screenshot tests with
  `./gradlew recordPaparazziDebug` and include them as part of the PR.
