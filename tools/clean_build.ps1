param([Parameter(Mandatory=$true)][ValidatePattern('^[0-9]+$')][string]$Version, [string]$ArtifactsRoot = '')
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
if (!$ArtifactsRoot) { $ArtifactsRoot = Join-Path (Split-Path $root -Parent) ((Split-Path $root -Leaf) + '_artifacts') }
$ArtifactsRoot = [IO.Path]::GetFullPath($ArtifactsRoot)
$build = Join-Path $ArtifactsRoot 'build'
if (!(Test-Path -LiteralPath $build)) { throw 'Missing build directory' }
function Assert-SafeEntry($entry) {
    $path = [IO.Path]::GetFullPath($entry.FullName)
    if (!$path.StartsWith($build + '\', [StringComparison]::OrdinalIgnoreCase)) { throw 'Outside build directory' }
    if ($entry.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Build links are not allowed' }
}
if ((Get-Item -LiteralPath $build).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Build directory is a link' }
$entries = @(Get-ChildItem -LiteralPath $build -Force)
foreach ($entry in $entries) {
    Assert-SafeEntry $entry
    if ($entry.PSIsContainer) { foreach ($child in Get-ChildItem -LiteralPath $entry.FullName -Recurse -Force) { Assert-SafeEntry $child } }
    if ($entry.Name -match '^uix_v([0-9]+)\.jar$' -and [long]$Matches[1] -gt [long]$Version) { throw 'A newer build exists; cleanup cancelled' }
}
# Require a readable executable JAR before deleting any previous result.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = [IO.Compression.ZipFile]::OpenRead((Join-Path $build "uix_v$Version.jar"))
try { if (!$jar.GetEntry('timeescape/Main.class') -or !$jar.GetEntry('META-INF/MANIFEST.MF')) { throw 'Incomplete build JAR' } } finally { $jar.Dispose() }
$keep = @("uix_v$Version.jar", "v$Version-classes", "v$Version-tests")
$targets = @($entries | Where-Object { $_.Name -notin $keep -and $_.Name -match '^(uix_v[0-9]+\.jar|v[0-9]+-(classes|tests)|test[0-9]+|classes|test-classes|checks|intro-check|travel-check|team-guide-example|weather-tools|\.staging-[a-f0-9]+)$' })
foreach ($entry in $targets) { Remove-Item -LiteralPath $entry.FullName -Recurse -Force }
Write-Output "Build cleanup complete: kept version $Version; removed $($targets.Count) old entries."
