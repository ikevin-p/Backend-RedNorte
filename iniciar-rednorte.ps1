# ════════════════════════════════════════════════════════════
#  RedNorte - Script de inicio automatico
#  Solo ejecutar este archivo y la aplicacion queda funcionando
# ════════════════════════════════════════════════════════════

$BACKEND_PATH  = "C:\Users\katty\Documents\backend-RedNorte"
$FRONTEND_PATH = "C:\Users\katty\Documents\RedNorte-frontend"
$DB_CONTAINER  = "cnt-db-rednorte"
$DB_PASS       = "11223344"

# Lista de seeds en el orden correcto de carga
$SEEDS = @(
    "02-seed-usuarios.sql",
    "03-seed-consultas.sql",
    "04-seed-fichas.sql",
    "05-seed-establecimientos.sql",
    "06-seed-agenda.sql",
    "07-seed-demo.sql"
)

Write-Host ""
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host "        INICIANDO REDNORTE" -ForegroundColor Magenta
Write-Host "=============================================" -ForegroundColor Magenta
Write-Host ""

# ─── PASO 1: Verificar Docker Desktop ───────────────────────
Write-Host "[1/7] Verificando Docker Desktop..." -ForegroundColor Cyan
$dockerOk = $false
try {
    docker info 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { $dockerOk = $true }
} catch { $dockerOk = $false }

if (-not $dockerOk) {
    Write-Host "      Docker no esta corriendo. Iniciandolo..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    Write-Host "      Esperando a que Docker arranque (puede tardar 1 min)..." -ForegroundColor Yellow
    $intentos = 0
    while ($intentos -lt 24) {
        Start-Sleep -Seconds 5
        docker info 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) { $dockerOk = $true; break }
        $intentos++
    }
}

if (-not $dockerOk) {
    Write-Host "[ERROR] Docker no respondio. Abre Docker Desktop manualmente y reintenta." -ForegroundColor Red
    pause
    exit 1
}
Write-Host "      [OK] Docker esta corriendo" -ForegroundColor Green
Write-Host ""

# ─── PASO 2: Levantar contenedores ──────────────────────────
Write-Host "[2/7] Levantando los 13 microservicios (docker compose)..." -ForegroundColor Cyan
Set-Location $BACKEND_PATH
docker compose down 2>&1 | Out-Null
docker compose up --build -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Fallo docker compose up. Revisa el docker-compose.yml" -ForegroundColor Red
    pause
    exit 1
}
Write-Host "      [OK] Contenedores construidos e iniciados" -ForegroundColor Green
Write-Host ""

# ─── PASO 3: Esperar a que la base de datos este lista ──────
Write-Host "[3/7] Esperando a que MySQL este disponible..." -ForegroundColor Cyan
$dbLista = $false
$intentos = 0
while ($intentos -lt 30) {
    Start-Sleep -Seconds 4
    docker exec $DB_CONTAINER mysqladmin ping -uroot -p$DB_PASS --silent 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { $dbLista = $true; break }
    Write-Host "      ...esperando MySQL ($($intentos*4)s)" -ForegroundColor DarkGray
    $intentos++
}

if (-not $dbLista) {
    Write-Host "[ERROR] MySQL no respondio a tiempo. Espera un poco y carga los seeds manualmente." -ForegroundColor Red
} else {
    Write-Host "      [OK] MySQL listo" -ForegroundColor Green
}
Write-Host ""

# ─── PASO 4: Esperar a que Eureka tenga los microservicios registrados ─
# En vez de un sleep fijo, se consulta el propio Eureka hasta ver que
# al menos 10 instancias estan registradas (los 9 microservicios
# originales + ms-chatbot; ms-usuarios queda fuera, es esperado).
Write-Host "[4/7] Esperando a que los microservicios se registren en Eureka..." -ForegroundColor Cyan
$MIN_INSTANCIAS_ESPERADAS = 10
$eurekaListo = $false
$intentos = 0
while ($intentos -lt 30) {
    Start-Sleep -Seconds 5
    try {
        $resp = Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{Accept="application/json"} -TimeoutSec 5
        $apps = $resp.applications.application
        $cantidad = if ($apps) { @($apps).Count } else { 0 }
        Write-Host "      ...$cantidad/$MIN_INSTANCIAS_ESPERADAS microservicios registrados ($($intentos*5)s)" -ForegroundColor DarkGray
        if ($cantidad -ge $MIN_INSTANCIAS_ESPERADAS) { $eurekaListo = $true; break }
    } catch {
        Write-Host "      ...Eureka aun no responde ($($intentos*5)s)" -ForegroundColor DarkGray
    }
    $intentos++
}

if ($eurekaListo) {
    Write-Host "      [OK] Microservicios registrados en Eureka" -ForegroundColor Green
} else {
    Write-Host "      [!] No se alcanzo el minimo esperado a tiempo, pero se continua" -ForegroundColor Yellow
    Write-Host "      [!] Revisa http://localhost:8761 manualmente si algo falta" -ForegroundColor Yellow
}
Write-Host ""

# ─── PASO 5: Cargar seeds (metodo UTF-8 seguro) ─────────────
# Se usa 'docker cp' + 'source' porque el pipe (Get-Content |) corrompe
# las tildes y la enie. Este metodo respeta los caracteres en espanol.
Write-Host "[5/7] Cargando datos de prueba en la base de datos..." -ForegroundColor Cyan
foreach ($seed in $SEEDS) {
    $ruta = Join-Path $BACKEND_PATH $seed
    if (Test-Path $ruta) {
        docker cp $ruta "${DB_CONTAINER}:/tmp/$seed" 2>&1 | Out-Null
        docker exec $DB_CONTAINER mysql -uroot -p$DB_PASS --default-character-set=utf8mb4 -e "source /tmp/$seed" 2>&1 | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "      [OK] $seed cargado" -ForegroundColor Green
        } else {
            Write-Host "      [!] $seed dio un aviso (puede ser normal si ya estaban los datos)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "      [!] No se encontro $seed (se omite)" -ForegroundColor Yellow
    }
}
Write-Host "      [OK] Carga de datos finalizada" -ForegroundColor Green
Write-Host ""

# ─── PASO 6: Verificar que Ollama este corriendo (SaludBot) ──
# ms-chatbot llama a Ollama en http://host.docker.internal:11434 desde
# dentro del contenedor. Ollama NO se levanta con docker compose (corre
# directo en Windows), asi que si no esta activo, SaludBot respondera
# con el mensaje de fallback del Gateway en vez de conversar de verdad.
# Este paso solo informa el estado, no bloquea el arranque del resto.
Write-Host "[6/7] Verificando Ollama (necesario para SaludBot)..." -ForegroundColor Cyan
$ollamaOk = $false
try {
    $resp = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -TimeoutSec 5
    $modelos = @($resp.models | ForEach-Object { $_.name })
    if ($modelos -contains "llama3.2:3b") {
        $ollamaOk = $true
        Write-Host "      [OK] Ollama esta corriendo con llama3.2:3b disponible" -ForegroundColor Green
    } elseif ($modelos.Count -gt 0) {
        Write-Host "      [!] Ollama esta corriendo, pero llama3.2:3b no esta descargado" -ForegroundColor Yellow
        Write-Host "      [!] Modelos disponibles: $($modelos -join ', ')" -ForegroundColor Yellow
        Write-Host "      [!] Ejecuta: ollama pull llama3.2:3b" -ForegroundColor Yellow
    } else {
        Write-Host "      [!] Ollama responde pero no tiene ningun modelo descargado" -ForegroundColor Yellow
    }
} catch {
    Write-Host "      [!] Ollama no esta corriendo (SaludBot no funcionara hasta que lo inicies)" -ForegroundColor Yellow
    Write-Host "      [!] Abre otra terminal y ejecuta: ollama serve" -ForegroundColor Yellow
}
Write-Host ""

# ─── PASO 7: Iniciar Frontend React ─────────────────────────
Write-Host "[7/7] Iniciando el frontend React..." -ForegroundColor Cyan
if (-not (Test-Path (Join-Path $FRONTEND_PATH "node_modules"))) {
    Write-Host "      node_modules no existe. Instalando dependencias (npm install)..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$FRONTEND_PATH'; npm install; npm start" -WindowStyle Normal
} else {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$FRONTEND_PATH'; npm start" -WindowStyle Normal
}
Write-Host "      [OK] Frontend arrancando en una ventana nueva" -ForegroundColor Green
Write-Host "      (espera ~20s a que compile y abra el navegador solo)" -ForegroundColor DarkGray
Write-Host ""

# ─── Abrir paneles utiles en el navegador ───────────────────
Start-Sleep -Seconds 18
Start-Process "http://localhost:8761"
Start-Process "http://localhost:3000"

# ─── Resumen final ──────────────────────────────────────────
Write-Host "=============================================" -ForegroundColor Green
Write-Host "        REDNORTE ESTA LISTO" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""
Write-Host " URLs del sistema:" -ForegroundColor White
Write-Host "   Frontend     -> http://localhost:3000" -ForegroundColor Cyan
Write-Host "   Eureka       -> http://localhost:8761" -ForegroundColor Cyan
Write-Host "   API Gateway  -> http://localhost:8090" -ForegroundColor Cyan
Write-Host "   Swagger      -> http://localhost:8080/swagger-ui/index.html (cada microservicio tiene el suyo en su puerto)" -ForegroundColor Cyan
Write-Host ""
Write-Host " SaludBot (chatbot con IA):" -ForegroundColor White
if ($ollamaOk) {
    Write-Host "   [OK] Listo para usarse desde el icono flotante en el sitio" -ForegroundColor Green
} else {
    Write-Host "   [!] Ollama no esta corriendo: el bot mostrara un mensaje de 'no disponible'" -ForegroundColor Yellow
    Write-Host "   [!] Para activarlo: abre una terminal y ejecuta 'ollama serve'" -ForegroundColor Yellow
}
Write-Host ""
Write-Host " Cuentas de prueba:" -ForegroundColor White
Write-Host "   Admin     -> admin@rednorte.cl     / admin123" -ForegroundColor Yellow
Write-Host "   Doctor    -> dr.vega@rednorte.cl   / doctor123" -ForegroundColor Yellow
Write-Host "   Paciente  -> juan.perez@correo.cl  / paciente123" -ForegroundColor Yellow
Write-Host ""
Write-Host " Para apagar todo cuando termines, ejecuta:" -ForegroundColor White
Write-Host "   docker compose down" -ForegroundColor DarkGray
Write-Host ""
pause
