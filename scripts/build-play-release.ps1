param([string]$SigningDirectory)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (!$SigningDirectory) { $SigningDirectory = Join-Path (Split-Path $projectRoot -Parent) 'SleepGuard-PrivateSigning' }
$keyPath = Join-Path $SigningDirectory 'sleepguard-upload.p12'
$passwordPath = Join-Path $SigningDirectory 'upload-password.encrypted'
if (!(Test-Path -LiteralPath $keyPath) -or !(Test-Path -LiteralPath $passwordPath)) {
    throw 'Upload signing files are missing. They must be restored separately; never store them in Git.'
}
$previousPassword = $env:SLEEP_GUARD_UPLOAD_PASSWORD
$previousStore = $env:SLEEP_GUARD_UPLOAD_STORE_FILE
try {
    $securePassword = Get-Content -LiteralPath $passwordPath | ConvertTo-SecureString
    $env:SLEEP_GUARD_UPLOAD_PASSWORD = [Net.NetworkCredential]::new('', $securePassword).Password
    $env:SLEEP_GUARD_UPLOAD_STORE_FILE = (Resolve-Path $keyPath).Path
    & (Join-Path $projectRoot 'gradlew.bat') -p $projectRoot :app:bundleRelease :app:assembleRelease :app:testReleaseUnitTest :app:lintRelease --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Release build or checks failed.' }
} finally {
    $env:SLEEP_GUARD_UPLOAD_PASSWORD = $previousPassword
    $env:SLEEP_GUARD_UPLOAD_STORE_FILE = $previousStore
    $securePassword = $null
}
