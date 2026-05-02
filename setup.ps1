# AlohaClient - Setup Script

Write-Host ""
Write-Host "  AlohaClient Setup" -ForegroundColor Cyan
Write-Host "  =========================" -ForegroundColor Cyan
Write-Host ""

# 1. Check Java
Write-Host "[1/2] Checking Java..." -ForegroundColor Yellow

$javaExe = Get-Command java -ErrorAction SilentlyContinue
if ($javaExe) {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    Write-Host "  OK: Java found - $javaVersion" -ForegroundColor Green
} else {
    Write-Host "  ERROR: Java not found!" -ForegroundColor Red
    Write-Host "  Install Java 16+ from: https://adoptium.net/" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# 2. Download gradle-wrapper.jar
Write-Host ""
Write-Host "[2/2] Downloading gradle-wrapper.jar..." -ForegroundColor Yellow

$jarDir  = Join-Path $PSScriptRoot "gradle\wrapper"
$jarPath = Join-Path $jarDir "gradle-wrapper.jar"

if (Test-Path $jarPath) {
    $size = (Get-Item $jarPath).Length
    Write-Host "  OK: gradle-wrapper.jar already exists ($size bytes)" -ForegroundColor Green
} else {
    New-Item -ItemType Directory -Force -Path $jarDir | Out-Null

    $url = "https://github.com/gradle/gradle/raw/v7.4.0/gradle/wrapper/gradle-wrapper.jar"
    Write-Host "  Downloading from: $url" -ForegroundColor DarkGray

    $webClient = New-Object System.Net.WebClient
    $webClient.DownloadFile($url, $jarPath)

    if ((Test-Path $jarPath) -and ((Get-Item $jarPath).Length -gt 1000)) {
        $size = (Get-Item $jarPath).Length
        Write-Host "  OK: gradle-wrapper.jar downloaded ($size bytes)" -ForegroundColor Green
    } else {
        Write-Host "  ERROR: Download failed." -ForegroundColor Red
        Write-Host "  Download manually from:" -ForegroundColor Yellow
        Write-Host "  $url" -ForegroundColor Cyan
        Write-Host "  Place the file in:" -ForegroundColor Yellow
        Write-Host "  $jarDir" -ForegroundColor Cyan
        Read-Host "Press Enter to exit"
        exit 1
    }
}

Write-Host ""
Write-Host "  =========================" -ForegroundColor Cyan
Write-Host "  Done! Now run:" -ForegroundColor Green
Write-Host ""
Write-Host "      gradlew.bat build" -ForegroundColor White
Write-Host ""
Write-Host "  Output JAR: build\libs\alohaclient-1.0.0.jar" -ForegroundColor DarkGray
Write-Host "  =========================" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit"
