#!/usr/bin/env bash

# Assume current working directory = root of kur-blog repo
release_dir='release'

# help
if [[ $# != 2 ]]
then
    echo 'Release current version - save and commit version info, add tag'
    echo 'Usage: ./script/release.sh <version-tag> <tag-message>'
    exit 1
fi

# Working tree should be clean
if [[ $(git diff --stat) != '' ]]
then 
    echo 'Working tree should be clean. Commit changes first!'
    exit 1
fi

# Build target
clj -T:build release

# Save version metadata
echo "tag: $1"                                  > "$release_dir/version.yml"
echo "summary: $2"                              >> "$release_dir/version.yml"
echo "hash: `git rev-parse HEAD`"               >> "$release_dir/version.yml"
echo "short_hash: `git rev-parse --short HEAD`" >> "$release_dir/version.yml"

# Commit and set tag
git add "$release_dir/version.yml"
git commit -m "Release kur-blog, version: $1"
git tag -a "$1" -m "$2"
