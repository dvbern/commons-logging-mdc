#!/bin/bash
set -euo pipefail

### parameters ############################################

if [ "${1:-}" == "--push" ] || [ "${1:-}" == "-p" ]; then
  do_push=true
else
  do_push=false
fi

### functions #############################################

function commit() {
  local message="$*"
  git add .
  git commit -m "Release: ${message}"
}

function print_project_version() {
  ./mvnw org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version \
  | sed -n -e '/^\[.*\]/ !{ /^[0-9]/ { p; q } }'
}

### main ##################################################
echo "Check if directory is clean" >&2
if [ -n "$(git status --porcelain)" ]; then
  echo "Directory contains uncommitted changes" >&2
  exit 1;
fi

echo "Check if local branch is behind remote branch" >&2
git fetch
behind_count=$(git rev-list --count "HEAD..@{u}")
if [ "$behind_count" -ne "0" ]; then
  echo "Local branch is behind remote branch by $behind_count commits" >&2
  exit 1;
fi

echo "Remove SNAPSHOT from version" >&2
./mvnw versions:set -DremoveSnapshot=true
version=$(print_project_version)
commit "Set release version to: ${version}"

version_tag="v${version}"
echo "Git tag: ${version_tag}" >&2
git tag "${version_tag}"

echo "Set next SNAPSHOT version" >&2
./mvnw versions:set -DnextSnapshot=true
snapshot_version=$(print_project_version)
commit "Set next SNAPSHOT version: ${snapshot_version}"

echo "Push to git" >&2
if [ "${do_push}" = true ]; then
  echo "Push to git..." >&2
  git push origin "${version_tag}"
  # let gitlab trigger building the tag and thus prevent the next development version from being built first
  sleep 1
  git push origin
else
  echo "Skipping push to git" >&2
fi
