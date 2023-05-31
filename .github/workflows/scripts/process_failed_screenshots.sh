#!/bin/bash

if [[ -z ${GITHUB_TOKEN} ]]; then
  echo "Missing GITHUB_TOKEN variable"
  exit 1
fi

if [[ -z ${GITHUB_REPOSITORY} ]]; then
  echo "Missing GITHUB_REPOSITORY variable"
  exit 1
fi

if [[ -z ${PR_BRANCH} ]]; then
  echo "Missing PR_BRANCH variable"
  exit 1
fi

./gradlew recordPaparazziDebug

PR_NUMBER=${GITHUB_REF#refs/pull/}
PR_NUMBER=${PR_NUMBER/\/merge/}
NEW_BRANCH_NAME="snapshots/pr-$PR_NUMBER"
echo "::set-output name=PR_NUMBER::$PR_NUMBER"

git config user.name "ElementBot"
git config user.email "benoitm+elementbot@element.io"
git fetch --all
git checkout --track "origin/$PR_BRANCH"
git checkout -b "$NEW_BRANCH_NAME"
git add -A
git commit -m "Update screenshots"
git push --force "https://$GITHUB_TOKEN@github.com/$GITHUB_REPOSITORY.git"
echo "::set-output name=PR_COMMENT::\"Screenshot tests failed.\\n\\n[See differences](https://github.com/$GITHUB_REPOSITORY/compare/$PR_BRANCH...$NEW_BRANCH_NAME)\\n\\nMerge the branch if it's an intentional change.\""
