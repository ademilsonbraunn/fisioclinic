# ==============================================================
#  FisioClinic -- Setup do banco de dados local
#  PostgreSQL 11 -- execute este script no seu terminal
#  Uso: .\setup.ps1
# ==============================================================

$PG_BIN  = "C:\Program Files\PostgreSQL\11\bin"
$PSQL    = "$PG_BIN\psql.exe"
$SCHEMA  = "$PSScriptRoot\schema.sql"
$DB_NAME = "fisioclinic"
$DB_USER = "fisio"
$DB_PASS = "fisio123"
$PG_PORT = "5411"

Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  FisioClinic -- Configuracao do banco de dados" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""

# Solicita a senha do superusuario postgres
$securePass = Read-Host "Digite a senha do usuario postgres" -AsSecureString
$BSTR    = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePass)
$PG_PASS = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
[System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR)

$env:PGPASSWORD = $PG_PASS

# --------------------------------------------------------------
# 1. Verifica conexao
# --------------------------------------------------------------
Write-Host "[1/4] Verificando conexao com o PostgreSQL na porta $PG_PORT..." -ForegroundColor Yellow

$result = & $PSQL -U postgres -p $PG_PORT -c "SELECT 1;" -t -q 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERRO: Nao foi possivel conectar ao PostgreSQL." -ForegroundColor Red
    Write-Host "Verifique se o servico esta rodando na porta $PG_PORT e a senha esta correta." -ForegroundColor Red
    $env:PGPASSWORD = ""
    exit 1
}
Write-Host "     Conexao OK." -ForegroundColor Green

# --------------------------------------------------------------
# 2. Cria o usuario 'fisio'
# --------------------------------------------------------------
Write-Host "[2/4] Criando usuario '$DB_USER'..." -ForegroundColor Yellow

$checkUser = & $PSQL -U postgres -p $PG_PORT -t -q -c "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER';" 2>&1
$userExists = ($checkUser -join "") -match "1"

if ($userExists) {
    Write-Host "     Usuario '$DB_USER' ja existe -- pulando." -ForegroundColor DarkGray
} else {
    & $PSQL -U postgres -p $PG_PORT -q -c "CREATE ROLE $DB_USER WITH LOGIN PASSWORD '$DB_PASS';" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "     Usuario '$DB_USER' criado com sucesso." -ForegroundColor Green
    } else {
        Write-Host "     ERRO ao criar usuario '$DB_USER'." -ForegroundColor Red
        $env:PGPASSWORD = ""
        exit 1
    }
}

# --------------------------------------------------------------
# 3. Cria o banco 'fisioclinic'
# --------------------------------------------------------------
Write-Host "[3/4] Criando banco de dados '$DB_NAME'..." -ForegroundColor Yellow

$checkDb = & $PSQL -U postgres -p $PG_PORT -t -q -c "SELECT 1 FROM pg_database WHERE datname='$DB_NAME';" 2>&1
$dbExists = ($checkDb -join "") -match "1"

if ($dbExists) {
    Write-Host "     Banco '$DB_NAME' ja existe -- schema sera reaplicado." -ForegroundColor DarkGray
} else {
    & $PSQL -U postgres -p $PG_PORT -q -c "CREATE DATABASE $DB_NAME OWNER $DB_USER ENCODING 'UTF8' TEMPLATE template0;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "     Banco '$DB_NAME' criado com sucesso." -ForegroundColor Green
    } else {
        Write-Host "     ERRO ao criar banco '$DB_NAME'." -ForegroundColor Red
        $env:PGPASSWORD = ""
        exit 1
    }
}

& $PSQL -U postgres -p $PG_PORT -q -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;" 2>&1

# --------------------------------------------------------------
# 4. Aplica o schema
# --------------------------------------------------------------
Write-Host "[4/4] Aplicando schema..." -ForegroundColor Yellow

if (-not (Test-Path $SCHEMA)) {
    Write-Host "     ERRO: Arquivo schema.sql nao encontrado em: $SCHEMA" -ForegroundColor Red
    $env:PGPASSWORD = ""
    exit 1
}

$output = & $PSQL -U postgres -p $PG_PORT -d $DB_NAME -f $SCHEMA 2>&1
$exitCode = $LASTEXITCODE

$output | ForEach-Object { Write-Host "     $_" -ForegroundColor DarkGray }

if ($exitCode -eq 0) {
    Write-Host "     Schema aplicado com sucesso." -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "     AVISO: Alguns erros acima podem ser normais" -ForegroundColor DarkYellow
    Write-Host "     (ex: objeto ja existe se o schema foi aplicado antes)." -ForegroundColor DarkYellow
}

# Limpa senha da memoria
$env:PGPASSWORD = ""
$PG_PASS = ""

# --------------------------------------------------------------
# Resumo
# --------------------------------------------------------------
Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "  Setup concluido!" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Banco  : $DB_NAME"
Write-Host "  Usuario: $DB_USER"
Write-Host "  Senha  : $DB_PASS"
Write-Host "  Host   : localhost:$PG_PORT"
Write-Host ""

# Mostra tabelas criadas (conecta como fisio)
Write-Host "Tabelas criadas:" -ForegroundColor DarkGray
$env:PGPASSWORD = $DB_PASS
& $PSQL -U $DB_USER -p $PG_PORT -d $DB_NAME -c "\dt" 2>&1
$env:PGPASSWORD = ""

Write-Host ""
Write-Host "Proximo passo: cd backend && mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
