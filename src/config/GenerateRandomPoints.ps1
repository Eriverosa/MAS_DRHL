$dir = "C:\Users\eriveros\Documents\Proyectos\DRHL.SERV\MAS_DRHL\src\dataFiles"

# Poligono del area (mismo usado anteriormente)
$poligono = @(
    @(-20.2008582, -70.1358066),
    @(-20.2230876, -70.1321304),
    @(-20.2257050, -70.1307238),
    @(-20.2316685, -70.1283741),
    @(-20.2338586, -70.1283592),
    @(-20.2350538, -70.1285100),
    @(-20.2787756, -70.1243128),
    @(-20.2900637, -70.1210551),
    @(-20.2141433, -70.1571108),
    @(-20.2006785, -70.1382096),
    @(-20.2008582, -70.1358066)
)

# Exclusiones: Rotonda El Pampino (A-16) + zona actual (mala) de Transporte10
# (sector Huayquique / calle no deseada) con radio amplio para evitar volver
# a caer cerca del mismo lugar.
$exclusiones = @(
    @(-20.220772, -70.131701, 350),               # Rotonda El Pampino
    @(-20.26060860292633, -70.13324288724887, 900) # Zona actual mala de Transporte10 (radio 900m)
)

$minSepMetros  = 250
$maxSnapMetros = 60
$maxIntentos   = 500

# Puntos actuales de los OTROS 47 agentes, para no generar demasiado cerca de ellos
$otrosPuntos = @(
    @(-20.261955963784185,-70.12649611029673),
    @(-20.23039779543948,-70.14625756356595),
    @(-20.235681241736057,-70.14113128984637),
    @(-20.219568677208894,-70.14643986081502),
    @(-20.20348416925073,-70.1414603006826),
    @(-20.23115381502479,-70.13055098138959),
    @(-20.27541051477007,-70.1269047769906),
    @(-20.233989082538262,-70.14622350306523),
    @(-20.240192706800233,-70.13119352720061),
    @(-20.23035700033303,-70.1390767005765),
    @(-20.219589579471364,-70.1413915476623),
    @(-20.20649433116391,-70.1380198857143),
    @(-20.234008651890846,-70.14310234876415),
    @(-20.211367976452753,-70.14316015614698),
    @(-20.238442675890404,-70.14405616121182),
    @(-20.203873843500517,-70.1369341682778),
    @(-20.217971602826438,-70.1507324509796),
    @(-20.25276496157892,-70.13400286267819),
    @(-20.255670541601766,-70.12704807321484),
    @(-20.21853093153196,-70.14364415782168),
    @(-20.246396445513263,-70.1336915281035),
    @(-20.25458904308809,-70.13200595028546),
    @(-20.236043802578774,-70.1330769491358),
    @(-20.228004023612236,-70.13546846102417),
    @(-20.25063442392712,-70.13104237058373),
    @(-20.27266519680149,-70.1287681102482),
    @(-20.241352976179755,-70.14007674054022),
    @(-20.270572699370614,-70.12622224300904),
    @(-20.248295501257584,-70.13081148443756),
    @(-20.228718599048406,-70.14162841043832),
    @(-20.263125091197544,-70.12950758353703),
    @(-20.2153752006068,-70.14134992409173),
    @(-20.224612868639586,-70.13311593539419),
    @(-20.220729509220007,-70.14903091201458),
    @(-20.227750207287233,-70.14786426056983),
    @(-20.215394985716756,-70.15240789422089),
    @(-20.21930615507085,-70.13495745094296),
    @(-20.232979412479597,-70.13685648921526),
    @(-20.249922580944606,-70.13540565435767),
    @(-20.22693003786394,-70.14486961682685),
    @(-20.244156610906728,-70.13833147863227),
    @(-20.208286017901592,-70.14511745655209),
    @(-20.26425373834608,-70.127298505101),
    @(-20.22415292368444,-70.14306886892263),
    @(-20.222580321122038,-70.13780847529628),
    @(-20.208284204137282,-70.14761336279528),
    @(-20.25108841102527,-70.12755047572387)
)

function DistanciaMetros($lat1,$lon1,$lat2,$lon2) {
    $R = 6371000.0
    $dLat = ($lat2 - $lat1) * [Math]::PI / 180
    $dLon = ($lon2 - $lon1) * [Math]::PI / 180
    $a = [Math]::Sin($dLat/2) * [Math]::Sin($dLat/2) + [Math]::Cos($lat1 * [Math]::PI / 180) * [Math]::Cos($lat2 * [Math]::PI / 180) * [Math]::Sin($dLon/2) * [Math]::Sin($dLon/2)
    $c = 2 * [Math]::Atan2([Math]::Sqrt($a), [Math]::Sqrt(1-$a))
    return $R * $c
}

function PuntoEnPoligono($lat, $lon, $poly) {
    $dentro = $false
    $n = $poly.Count
    $j = $n - 1
    for ($i = 0; $i -lt $n; $i++) {
        $yi = $poly[$i][0]; $xi = $poly[$i][1]
        $yj = $poly[$j][0]; $xj = $poly[$j][1]
        if ( (($yi -gt $lat) -ne ($yj -gt $lat)) -and
             ($lon -lt ($xj - $xi) * ($lat - $yi) / ($yj - $yi) + $xi) ) {
            $dentro = -not $dentro
        }
        $j = $i
    }
    return $dentro
}

$latMin = ($poligono | ForEach-Object { $_[0] } | Measure-Object -Minimum).Minimum
$latMax = ($poligono | ForEach-Object { $_[0] } | Measure-Object -Maximum).Maximum
$lonMin = ($poligono | ForEach-Object { $_[1] } | Measure-Object -Minimum).Minimum
$lonMax = ($poligono | ForEach-Object { $_[1] } | Measure-Object -Maximum).Maximum

$rand = New-Object System.Random

$encontrado = $null
for ($intento = 0; $intento -lt $maxIntentos; $intento++) {
    $lat = $latMin + ($rand.NextDouble() * ($latMax - $latMin))
    $lon = $lonMin + ($rand.NextDouble() * ($lonMax - $lonMin))

    if (-not (PuntoEnPoligono $lat $lon $poligono)) { continue }

    try {
        $r = Invoke-RestMethod -Uri "http://localhost:8989/nearest?point=$lat,$lon" -TimeoutSec 10
    } catch {
        continue
    }
    if ($r.distance -gt $maxSnapMetros) { continue }
    $nLat = $r.coordinates[1]
    $nLon = $r.coordinates[0]

    if (-not (PuntoEnPoligono $nLat $nLon $poligono)) { continue }

    $enExclusion = $false
    foreach ($ex in $exclusiones) {
        if ((DistanciaMetros $nLat $nLon $ex[0] $ex[1]) -lt $ex[2]) { $enExclusion = $true; break }
    }
    if ($enExclusion) { continue }

    $muyCerca = $false
    foreach ($p in $otrosPuntos) {
        if ((DistanciaMetros $nLat $nLon $p[0] $p[1]) -lt $minSepMetros) { $muyCerca = $true; break }
    }
    if ($muyCerca) { continue }

    $encontrado = @($nLat, $nLon)
    break
}

if ($encontrado -eq $null) {
    Write-Host "No se encontro un punto valido para Transporte10 tras $maxIntentos intentos." -ForegroundColor Red
} else {
    $latStr = $encontrado[0].ToString([System.Globalization.CultureInfo]::InvariantCulture)
    $lonStr = $encontrado[1].ToString([System.Globalization.CultureInfo]::InvariantCulture)
    Write-Host ("Nueva ubicacion para Transporte10:  " + $latStr + ", " + $lonStr) -ForegroundColor Green

    $path = Join-Path $dir "ArchivoAgente_Transporte.csv"
    $lineas = Get-Content $path
    $out = New-Object System.Collections.ArrayList
    foreach ($l in $lineas) {
        if ($l -match "^Transporte10;") {
            $c = $l -split ";"
            $c[1] = $latStr
            $c[2] = $lonStr
            $out.Add(($c -join ";")) | Out-Null
            Write-Host ("Linea actualizada: " + ($c -join ";"))
        } else {
            $out.Add($l) | Out-Null
        }
    }
    $noBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($path, $out.ToArray(), $noBom)
    Write-Host "Archivo actualizado. Solo Transporte10 cambio, los otros 47 puntos quedaron intactos." -ForegroundColor Green
}
