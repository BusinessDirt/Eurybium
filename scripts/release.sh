#!/bin/sh
# Create and push a release tag using the root gradle.properties mod_version.
set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
repo_dir=$(CDPATH= cd -- "$script_dir/.." && pwd)

version=$(awk '
    /^[[:space:]]*mod_version[[:space:]]*=/ {
        value = $0
        sub(/^[^=]*=/, "", value)
        sub(/^[[:space:]]*/, "", value)
        sub(/[[:space:]]*$/, "", value)
        count++
    }
    END {
        if (count != 1 || value == "") {
            print "Expected exactly one non-empty mod_version in gradle.properties" > "/dev/stderr"
            exit 1
        }
        print value
    }
' "$repo_dir/gradle.properties")

if ! printf '%s\n' "$version" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z]+([.-][0-9A-Za-z]+)*)?$'; then
    echo 'mod_version must be a release version such as 2.0.0 or 2.1.0-rc.1' >&2
    exit 1
fi

if [ -n "$(git -C "$repo_dir" status --porcelain)" ]; then
    echo 'Commit and push your changes before creating a release tag.' >&2
    exit 1
fi

tag="v$version"
printf 'Creating and pushing %s\n' "$tag"
git -C "$repo_dir" tag "$tag"
git -C "$repo_dir" push origin "refs/tags/$tag"
