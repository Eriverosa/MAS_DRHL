$ErrorActionPreference = "Continue"
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

# --- Asegurar que GraphHopper este arriba si algun experimento lo necesita ---
$necesitaGraphHopper = (@($experiments | Where-Object { $_.travelModel -eq "GRAPH_HOPPER" })).Count -gt 0
$ghLanzadoPorNosotros = $false
$ghProcess = $null
if ($necesitaGraphHopper) {
    $ghUp = $false
    try {
        Invoke-RestMethod -Uri "http://localhost:8989/health" -TimeoutSec 3 | Out-Null
        $ghUp = $true
    } catch { $ghUp = $false }

    if (-not $ghUp) {
        Write-Host "GraphHopper no esta corriendo. Levantando servidor..." -ForegroundColor Yellow
        $ghDir = "C:\Users\eriveros\Documents\GraphHopper"
        $ghLogOut = Join-Path $ghDir "graphhopper_startup.log"
        $ghLogErr = Join-Path $ghDir "graphhopper_startup.err.log"
        Remove-Item $ghLogOut -Force -EA SilentlyContinue
        Remove-Item $ghLogErr -Force -EA SilentlyContinue

        try {
            # IMPORTANTE: no se puede combinar -WindowStyle Normal con
            # -RedirectStandardOutput/-Error (Windows no permite mostrar una
            # ventana Y redirigir su salida al mismo tiempo). Se lanza oculto
            # y el log queda en disco para poder diagnosticar si algo falla.
            $ghProcess = Start-Process -FilePath "java" -ArgumentList "-Xmx4g","-jar","graphhopper-web.jar","server","config.yml" `
                -WorkingDirectory $ghDir -WindowStyle Hidden `
                -RedirectStandardOutput $ghLogOut -RedirectStandardError $ghLogErr -PassThru
            $ghLanzadoPorNosotros = $true
        } catch {
            Write-Host "ERROR al lanzar GraphHopper: $($_.Exception.Message)" -ForegroundColor Red
        }

        $intentos = 0
        $maxIntentos = 60
        while (-not $ghUp -and $intentos -lt $maxIntentos) {
            Start-Sleep -Seconds 2
            $intentos++
            try {
                Invoke-RestMethod -Uri "http://localhost:8989/health" -TimeoutSec 3 | Out-Null
                $ghUp = $true
            } catch { }
            Write-Host "." -NoNewline
        }
        Write-Host ""
        if ($ghUp) {
            Write-Host "GraphHopper listo." -ForegroundColor Green
        } else {
            Write-Host "GraphHopper no respondio a tiempo. Mostrando el log de arranque:" -ForegroundColor Red
            Write-Host "----- $ghLogErr -----" -ForegroundColor Red
            if (Test-Path $ghLogErr) { Get-Content $ghLogErr | Select-Object -Last 40 }
            Write-Host "----- $ghLogOut -----" -ForegroundColor Red
            if (Test-Path $ghLogOut) { Get-Content $ghLogOut | Select-Object -Last 40 }
        }
    } else {
        Write-Host "GraphHopper ya estaba corriendo." -ForegroundColor Green
    }
}

# Carpeta unica de resultados: se limpia una sola vez
$resultsDir = Join-Path $root "src\results"
$intentosBorrado = 0
while ((Test-Path $resultsDir) -and $intentosBorrado -lt 5) {
    try {
        Remove-Item $resultsDir -Recurse -Force -ErrorAction Stop
    } catch {
        $intentosBorrado++
        Write-Host "No se pudo limpiar $resultsDir (intento $intentosBorrado), reintentando..." -ForegroundColor Yellow
        Start-Sleep -Seconds 2
    }
}
New-Item -ItemType Directory -Force -Path $resultsDir | Out-Null

# Log unico de toda la ejecucion (todos los modelos)
$logPath = Join-Path $resultsDir "LOG.txt"
Remove-Item $logPath -Force -EA SilentlyContinue

# Si una corrida anterior en esta MISMA sesion quedo interrumpida (Ctrl+C),
# el StreamWriter puede seguir vivo en la variable de sesion, bloqueando el
# archivo. Se cierra explicitamente antes de intentar crear uno nuevo.
if ($null -ne $logWriter) {
    try { $logWriter.Close() } catch {}
    try { $logWriter.Dispose() } catch {}
    $logWriter = $null
}

# Un unico StreamWriter abierto durante toda la ejecucion (UTF-8, sin bloqueos)
try {
    $logWriter = New-Object System.IO.StreamWriter($logPath, $false, (New-Object System.Text.UTF8Encoding($false)))
} catch {
    Write-Host "ERROR: no se pudo crear $logPath" -ForegroundColor Red
    Write-Host "Motivo: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Cierra cualquier otra ventana de PowerShell o proceso Java que este corriendo y vuelve a intentar." -ForegroundColor Yellow
    exit 1
}
$logWriter.AutoFlush = $true

# Iterar cada experimento en JVM LIMPIA
foreach ($exp in $experiments) {
    $model = $exp.travelModel
    Write-Host "`n========================================" -ForegroundColor Magenta
    Write-Host "  EJECUTANDO MODELO: $model" -ForegroundColor Magenta
    Write-Host "========================================" -ForegroundColor Magenta

    $logWriter.WriteLine("")
    $logWriter.WriteLine("========================================")
    $logWriter.WriteLine("  MODELO: $model")
    $logWriter.WriteLine("========================================")

    java -DtravelModel="$model" -cp "$root\bin;$cp" src.Main *>&1 | ForEach-Object { $linea = $_ | Out-String -Stream; foreach ($l in $linea) { Write-Host $l; $logWriter.WriteLine($l) } }
}

$logWriter.Close()
$logWriter.Dispose()

Write-Host "`nGenerando reporte HTML..." -ForegroundColor Yellow
java -cp "$root\bin;$cp" src.services.ReportGenerator

# Apagar GraphHopper SOLO si este mismo script lo levanto.
# Si ya estaba corriendo de antes (por ejemplo, lo dejaste abierto tu manualmente),
# no lo tocamos, para no interrumpir otro uso que le estes dando.
if ($ghLanzadoPorNosotros -and $ghProcess -and -not $ghProcess.HasExited) {
    Write-Host "`nApagando GraphHopper (fue levantado por esta ejecucion)..." -ForegroundColor Yellow
    try {
        Stop-Process -Id $ghProcess.Id -Force -ErrorAction Stop
        Write-Host "GraphHopper detenido." -ForegroundColor Green
    } catch {
        Write-Host "No se pudo detener GraphHopper (PID $($ghProcess.Id)): $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`nTodos los experimentos finalizados." -ForegroundColor Green
Write-Host ("Log completo en: " + $logPath) -ForegroundColor DarkGray