param([string[]]$Tasks = @(':app:assembleDebug', ':app:lintDebug'))
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $keyPath = Join-Path $PSScriptRoot 'signing/bridge-debug.keystore'
    if (-not (Test-Path $keyPath)) {
        New-Item -ItemType Directory -Force (Split-Path $keyPath) | Out-Null
        $keytoolPath = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/keytool.exe' } else { 'keytool' }
        & $keytoolPath -genkeypair -keystore $keyPath -storepass android -alias androiddebugkey -keypass android -dname 'CN=Bili Client Bridge Debug' -keyalg RSA -keysize 2048 -validity 10000
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
    & ./gradlew.bat @Tasks --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally { Pop-Location }
