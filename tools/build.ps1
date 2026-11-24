param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
if (!$JavaHome) { throw 'Set JAVA_HOME to JDK 21 or newer.' }
$root = Split-Path $PSScriptRoot -Parent
$build = Join-Path $root 'build'
$classes = Join-Path $build 'classes'
# Verify the generated directory before cleaning stale class files.
if (Test-Path $classes) {
    $resolved = (Resolve-Path $classes).Path
    if ($resolved -ne [IO.Path]::GetFullPath($classes) -or !$resolved.StartsWith($root + '\')) { throw 'Unsafe build path' }
    Remove-Item -LiteralPath $resolved -Recurse -Force
}
New-Item -ItemType Directory -Force $classes | Out-Null
$files = @(Get-ChildItem (Join-Path $root 'src') -Filter *.java -Recurse | ForEach-Object FullName)
& "$JavaHome\bin\javac.exe" -encoding UTF-8 --release 21 -d $classes $files
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
# Generate approved weather data at build time, never during game startup.
$weatherClasses = Join-Path $build 'weather-tools'
New-Item -ItemType Directory -Force $weatherClasses | Out-Null
$weatherSources = @(Get-ChildItem (Join-Path $PSScriptRoot 'weather') -Filter *.java | ForEach-Object FullName)
& "$JavaHome\bin\javac.exe" -encoding UTF-8 --release 21 -cp $classes -d $weatherClasses $weatherSources
if ($LASTEXITCODE -ne 0) { throw 'Weather tools compilation failed' }
& "$JavaHome\bin\java.exe" '-Djava.awt.headless=true' -cp "$weatherClasses;$classes;$root\src" timeescape.ui.GenerateMenuWeather "$root\src\assets"
if ($LASTEXITCODE -ne 0) { throw 'Weather asset generation failed' }
& "$JavaHome\bin\jar.exe" "-J-Djava.io.tmpdir=$build" --create --file "$build\uix_v69.jar" --main-class timeescape.Main -C $classes . -C "$root\src" assets
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed' }
Write-Output "Built $build\uix_v69.jar"


