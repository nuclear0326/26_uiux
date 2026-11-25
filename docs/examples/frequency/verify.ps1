param([string]$JavaHome=$env:JAVA_HOME, [string]$ArtifactsRoot='')
$ErrorActionPreference='Stop'
if(!$JavaHome){throw 'Set JAVA_HOME to JDK 21.'}
$root=Split-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) -Parent
if(!$ArtifactsRoot){$ArtifactsRoot=Join-Path (Split-Path $root -Parent) ((Split-Path $root -Leaf)+'_artifacts')}
$classes=Join-Path $ArtifactsRoot 'build/team-guide-example'
New-Item -ItemType Directory -Force $classes | Out-Null
$sources=@(Get-ChildItem (Join-Path $root 'src') -Recurse -Filter *.java | ForEach-Object FullName)
$examples=@(Get-ChildItem $PSScriptRoot -Filter *.java | ForEach-Object FullName)
& "$JavaHome/bin/javac.exe" -encoding UTF-8 --release 21 -d $classes $sources $examples
if($LASTEXITCODE -ne 0){throw 'Example compilation failed'}
& "$JavaHome/bin/java.exe" '-Djava.awt.headless=true' -cp "$classes;$root/src" timeescape.ui.VerifyFrequencyExample
if($LASTEXITCODE -ne 0){throw 'Example verification failed'}
