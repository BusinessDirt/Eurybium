#!/usr/bin/env pwsh
# Create and push a release tag using the root gradle.properties mod_version.
$ErrorActionPreference = 'Stop'

$repoDir = Split-Path -Parent $PSScriptRoot
$propertiesPath = Join-Path $repoDir 'gradle.properties'
$versionLines = @(Get-Content -LiteralPath $propertiesPath | Where-Object {
    $_ -match '^\s*mod_version\s*='
})
if ($versionLines.Count -ne 1) {
    throw 'Expected exactly one non-empty mod_version in gradle.properties'
}
$version = ($versionLines[0] -split '=', 2)[1].Trim()
if ($version -notmatch '^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z]+([.-][0-9A-Za-z]+)*)?$') {
    throw 'mod_version must be a release version such as 2.0.0 or 2.1.0-rc.1'
}

$status = @(git -C $repoDir status --porcelain)
if ($LASTEXITCODE -ne 0) {
    throw 'Could not read the Git working tree status'
}
if ($status.Count -gt 0) {
    throw 'Commit and push your changes before creating a release tag.'
}

$tag = "v$version"
Write-Host "Creating and pushing $tag"
git -C $repoDir tag $tag
if ($LASTEXITCODE -ne 0) {
    throw "Could not create $tag; check whether the tag already exists"
}
git -C $repoDir push origin "refs/tags/$tag"
if ($LASTEXITCODE -ne 0) {
    throw "Could not push $tag. The local tag remains; retry with: git push origin refs/tags/$tag"
}
