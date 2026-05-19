# RedNorte - Script de inicio
$BACKEND_PATH  = "C:\Users\katty\Documents\backend-RedNorte"
$FRONTEND_PATH = "C:\Users\katty\Documents\RedNorte-frontend"
$URL_FRONTEND  = "http://localhost:3000"
$URL_EUREKA    = "http://localhost:8761"

Write-Host "=== INICIANDO REDNORTE ===" -ForegroundColor Magenta

# PASO 1: Docker
Write-Host "[1] Verificando Docker..." -ForegroundColor Cyan
$dp = Get-Process "Docker Desktop" -ErrorAction SilentlyContinue
if (-not $dp) {
    Write-Host "Iniciando Docker Desktop..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    Start-Sleep -Seconds 30
}
Write-Host "[OK] Docker listo" -ForegroundColor Green

# PASO 2: Contenedores
Write-Host "[2] Levantando contenedores..." -ForegroundColor Cyan
Set-Location $BACKEND_PATH
docker compose down 2>&1 | Out-Null
docker compose up --build -d
Write-Host "[OK] Contenedores iniciados" -ForegroundColor Green

# PASO 3: Esperar
Write-Host "[3] Esperando 45 segundos para que Spring Boot arranque..." -ForegroundColor Cyan
Start-Sleep -Seconds 45

# PASO 4: Seeds
Write-Host "[4] Cargando datos de prueba..." -ForegroundColor Cyan
Get-Content "$BACKEND_PATH\02-seed-usuarios.sql" | docker exec -i cnt-db-rednorte mysql -uroot -p11223344 2>&1
Get-Content "$BACKEND_PATH\03-seed-consultas.sql" | docker exec -i cnt-db-rednorte mysql -uroot -p11223344 2>&1
Write-Host "[OK] Datos cargados" -ForegroundColor Green

# PASO 5: Frontend
Write-Host "[5] Iniciando Frontend React..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit -Command Set-Location '$FRONTEND_PATH'; npm start"
Start-Sleep -Seconds 15

# PASO 6: Navegador
Write-Host "[6] Abriendo navegador..." -ForegroundColor Cyan
Start-Process $URL_FRONTEND
Start-Process $URL_EUREKA

Write-Host ""
Write-Host "=== REDNORTE LISTO ===" -ForegroundColor Green
Write-Host "Frontend    -> http://localhost:3000" -ForegroundColor Cyan
Write-Host "Eureka      -> http://localhost:8761" -ForegroundColor Cyan
Write-Host "API Gateway -> http://localhost:8090" -ForegroundColor Cyan
Write-Host "Admin       -> admin@rednorte.cl / admin123" -ForegroundColor Yellow
Write-Host "Doctor      -> dr.vega@rednorte.cl / doctor123" -ForegroundColor Yellow
Write-Host "Paciente    -> juan.perez@correo.cl / paciente123" -ForegroundColor Yellow
Write-Host ""
pause
