param([string]$JavaHome = $env:JAVA_HOME, [ValidatePattern('^[0-9]+$')][string]$Version = '99', [string]$ArtifactsRoot = '')
$ErrorActionPreference = 'Stop'
if (!$JavaHome) { throw 'Set JAVA_HOME to JDK 21 or newer.' }
$root = Split-Path $PSScriptRoot -Parent
if (!$ArtifactsRoot) { $ArtifactsRoot = Join-Path (Split-Path $root -Parent) ((Split-Path $root -Leaf) + '_artifacts') }
$ArtifactsRoot = [IO.Path]::GetFullPath($ArtifactsRoot)
$build = Join-Path $ArtifactsRoot 'build'
# Compile in isolation; a failed build must not replace the last successful build.
New-Item -ItemType Directory -Force $build | Out-Null
if ((Get-Item -LiteralPath $build).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Build directory is a link' }
$stage = Join-Path $build ('.staging-' + [Guid]::NewGuid().ToString('N'))
$classes = Join-Path $stage 'classes'
New-Item -ItemType Directory -Force $classes | Out-Null
$files = @(Get-ChildItem (Join-Path $root 'src') -Filter *.java -Recurse | ForEach-Object FullName)
& "$JavaHome\bin\javac.exe" -encoding UTF-8 --release 21 -d $classes $files
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
# Generate approved weather data at build time, never during game startup.
$weatherClasses = Join-Path $stage 'weather-tools'
New-Item -ItemType Directory -Force $weatherClasses | Out-Null
$weatherSources = @(Get-ChildItem (Join-Path $PSScriptRoot 'weather') -Filter *.java | ForEach-Object FullName)
& "$JavaHome\bin\javac.exe" -encoding UTF-8 --release 21 -cp $classes -d $weatherClasses $weatherSources
if ($LASTEXITCODE -ne 0) { throw 'Weather tools compilation failed' }
& "$JavaHome\bin\java.exe" '-Djava.awt.headless=true' -cp "$weatherClasses;$classes;$root\src" timeescape.ui.GenerateClockGlass "$root\src\assets"
if ($LASTEXITCODE -ne 0) { throw 'Clock fracture generation failed' }
& "$JavaHome\bin\java.exe" '-Djava.awt.headless=true' -cp "$weatherClasses;$classes;$root\src" timeescape.ui.GenerateMenuWeather "$root\src\assets"
if ($LASTEXITCODE -ne 0) { throw 'Weather asset generation failed' }
& "$JavaHome\bin\java.exe" '-Djava.awt.headless=true' -cp "$weatherClasses;$classes;$root\src" timeescape.ui.GenerateLabWeather "$root\src\assets"
if ($LASTEXITCODE -ne 0) { throw 'Lab weather generation failed' }
& "$JavaHome\bin\jar.exe" "-J-Djava.io.tmpdir=$build" --create --file "$stage\uix_v$Version.jar" --main-class timeescape.Main -C $classes . -C "$root\src" assets
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed' }
# Publish only after compilation and JAR creation succeeded.
$destinationClasses = Join-Path $build "v$Version-classes"
if (Test-Path -LiteralPath $destinationClasses) {
    $target = (Resolve-Path -LiteralPath $destinationClasses).Path
    if ($target -ne [IO.Path]::GetFullPath($destinationClasses) -or !$target.StartsWith($build + '\')) { throw 'Unsafe class output path' }
    $all = @((Get-Item -LiteralPath $target)) + @(Get-ChildItem -LiteralPath $target -Recurse -Force)
    if ($all | Where-Object { $_.Attributes -band [IO.FileAttributes]::ReparsePoint }) { throw 'Class output contains a link' }
    Remove-Item -LiteralPath $target -Recurse -Force
}
# Both resolved paths remain within this project's build directory.
if (!(Resolve-Path -LiteralPath $classes).Path.StartsWith($build + '\')) { throw 'Unsafe staging path' }
Move-Item -LiteralPath $classes -Destination $destinationClasses
Move-Item -LiteralPath "$stage\uix_v$Version.jar" -Destination "$build\uix_v$Version.jar" -Force
& (Join-Path $PSScriptRoot 'clean_build.ps1') -Version $Version -ArtifactsRoot $ArtifactsRoot
Write-Output "Built $build\uix_v$Version.jar"


