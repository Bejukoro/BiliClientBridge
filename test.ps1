$ErrorActionPreference = 'Stop'
$classes = Join-Path $PSScriptRoot 'build/parser-tests'
New-Item -ItemType Directory -Force $classes | Out-Null
$javacPath = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/javac.exe' } else { 'javac' }
$javaPath = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
& $javacPath -encoding UTF-8 -d $classes "$PSScriptRoot/app/src/main/java/local/pilinara/bridge/LinkParser.java" "$PSScriptRoot/tests/LinkParserTest.java"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& $javaPath -cp $classes LinkParserTest
exit $LASTEXITCODE
