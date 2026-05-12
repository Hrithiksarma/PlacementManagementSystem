# setup.ps1
# One-time setup script for PRMS — downloads the MySQL JDBC driver
# Run this once from the prms\ folder before compiling.
#
# How to run:
#   Right-click setup.ps1 → "Run with PowerShell"
# OR open PowerShell in this folder and type:
#   .\setup.ps1

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " PRMS — One-Time Setup: MySQL JDBC Driver Download" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

$jarUrl  = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
$jarPath = "lib\mysql-connector-j-8.0.33.jar"

# Create lib folder if it doesn't exist
if (-not (Test-Path "lib")) {
    New-Item -ItemType Directory -Path "lib" | Out-Null
}

if (Test-Path $jarPath) {
    Write-Host "  JDBC driver already present: $jarPath" -ForegroundColor Green
    Write-Host "  No download needed." -ForegroundColor Green
} else {
    Write-Host "  Downloading MySQL Connector/J 8.0.33..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -UseBasicParsing
        $size = (Get-Item $jarPath).Length
        Write-Host "  Downloaded successfully! ($([math]::Round($size/1MB, 2)) MB)" -ForegroundColor Green
    } catch {
        Write-Host "  DOWNLOAD FAILED: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "  Manual download URL:" -ForegroundColor Yellow
        Write-Host "  $jarUrl" -ForegroundColor White
        Write-Host "  Save it to:  $jarPath" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "  Next steps:" -ForegroundColor Cyan
Write-Host "    1. Double-click compile.bat  (compiles Java sources)"
Write-Host "    2. Double-click run.bat      (launches the application)"
Write-Host ""
Write-Host "  Make sure MySQL Server is running before clicking run.bat!" -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to exit"
