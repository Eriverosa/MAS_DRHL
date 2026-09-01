param(
    [ValidateSet("HAVERSINE", "GRAPH_HOPPER")]
    [string]$Model,
    [switch]$SkipCompile
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$configPath = Join-Path $root "src\config\config.json"
$mainClass  = "Main"

if ($Model) {
    $json = Get-Content $configPath -Raw -Encoding UTF8
    $json = $json -replace '"travelModel"\s*:\s*"[^"]*"', "`"travelModel`": `"$Model`""
    Set-Content -Path $configPath -Value $json -Encoding UTF8
    Write-Host "Modelo de viaje fijado en: $Model" -ForegroundColor Cyan
}

$jars = Get-ChildItem -Path $root -Recurse -Filter *.jar -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
if (-not $jars) {
    Write-Host "ERROR: no se encontraron JARs." -ForegroundColor Red
    exit 1
}
$cp = $jars -join ";"

if (-not $SkipCompile) {
    Write-Host "Compilando..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path "$root\bin" | Out-Null
    $sourcesFile = Join-Path $root "sources.txt"
    $sources = Get-ChildItem -Path "$root\src" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
    # Escribir SIN BOM (evita el error "Invalid filename: ?C:\...")
    [System.IO.File]::WriteAllLines($sourcesFile, $sources)
    javac -encoding UTF-8 -cp $cp -d "$root\bin" "@$sourcesFile"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR de compilacion." -ForegroundColor Red
        Remove-Item $sourcesFile -ErrorAction SilentlyContinue
        exit 1
    }
    Remove-Item $sourcesFile -ErrorAction SilentlyContinue
    Write-Host "Compilacion OK." -ForegroundColor Green
}

Write-Host "Iniciando simulacion ($mainClass)..." -ForegroundColor Yellow
java -cp "$root\bin;$cp" $mainClass

