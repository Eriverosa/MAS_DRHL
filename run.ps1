$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$configPath = Join-Path $root "src\config\config.json"

# Classpath con todos los JARs
$jars = Get-ChildItem -Path $root -Recurse -Filter *.jar -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
$cp = $jars -join ";"

# Compilar una sola vez
Write-Host "Compilando..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$root\bin" | Out-Null
$sourcesFile = Join-Path $root "sources.txt"
$sources = Get-ChildItem -Path "$root\src" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
[System.IO.File]::WriteAllLines($sourcesFile, $sources)
javac -encoding UTF-8 -cp $cp -d "$root\bin" "@$sourcesFile"
if ($LASTEXITCODE -ne 0) { Write-Host "ERROR de compilacion." -ForegroundColor Red; Remove-Item $sourcesFile -EA SilentlyContinue; exit 1 }
Remove-Item $sourcesFile -EA SilentlyContinue
Write-Host "Compilacion OK." -ForegroundColor Green

# Leer los experimentos del config.json
$config = Get-Content $configPath -Raw | ConvertFrom-Json
$experiments = $config.experiments | Where-Object { $_.enabled }

Write-Host ("Experimentos habilitados: " + ($experiments.travelModel -join ", ")) -ForegroundColor Cyan

# Iterar cada experimento en JVM LIMPIA
foreach ($exp in $experiments) {
    $model = $exp.travelModel
    Write-Host "`n========================================" -ForegroundColor Magenta
    Write-Host "  EJECUTANDO MODELO: $model" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta

    # Carpeta de resultados por modelo (limpia)
    $modelResults = Join-Path $root "src\results\$model"
    Remove-Item $modelResults -Recurse -Force -EA SilentlyContinue
    New-Item -ItemType Directory -Force -Path $modelResults | Out-Null

    # JVM nueva por modelo -> estado limpio garantizado.
    # Se pasa el modelo activo por propiedad del sistema (-DtravelModel).
    java -DtravelModel="$model" -cp "$root\bin;$cp" Main
}

Write-Host "`nTodos los experimentos finalizados." -ForegroundColor Green