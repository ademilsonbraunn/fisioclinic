# FisioClinic

Sistema de gestão clínica para fisioterapia. Cobre o ciclo completo do paciente: cadastro, anamnese, plano de tratamento, agendamento, evolução clínica (SOAP) e alta.

**Versão atual:** v1.4

---

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 17 · Spring Boot 3.2 · Spring Security (JWT) · Spring Data JPA |
| Frontend | HTML5 · CSS3 · JavaScript ES6+ (sem framework) |
| Banco | PostgreSQL 11 |
| Build | Maven |

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 11 instalado localmente (porta **5411**)

---

## Setup do banco de dados

Execute o script PowerShell na raiz do projeto. Ele cria o usuário, o banco e aplica o schema automaticamente:

```powershell
cd database
.\setup.ps1
```

O script vai pedir a senha do superusuário `postgres` e então criar:

| Campo | Valor |
|-------|-------|
| Banco | `fisioclinic` |
| Usuário | `fisio` |
| Senha | `fisio123` |
| Porta | `5411` |

> O arquivo `database/setup.sql` contém o schema completo e pode ser aplicado manualmente via `psql` se preferir.

---

## Rodando o backend

```bash
cd backend
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

> Na primeira execução o Spring pode levar alguns segundos a mais para conectar ao banco. Aguarde a mensagem `Started FisioclinicApplication`.

---

## Rodando o frontend

O frontend é HTML/CSS/JS puro — não há build step. Abra os arquivos diretamente com qualquer servidor estático:

**VS Code — Live Server (recomendado):**
1. Instale a extensão [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer)
2. Clique com o botão direito em `frontend/index.html` → **Open with Live Server**
3. O frontend abre em `http://127.0.0.1:5500`

**Alternativa via Python:**
```bash
cd frontend
python -m http.server 5500
```

---

## Login padrão

| Campo | Valor |
|-------|-------|
| E-mail | `admin@fisioclinic.com` |
| Senha | `admin123` |

---

## Módulos

| # | Módulo | Status |
|---|--------|--------|
| 1 | Cadastro do paciente | ✅ Concluído |
| 2 | Anamnese e avaliação inicial | 🔨 Em andamento |
| 3 | Diagnóstico e plano de tratamento | 🔲 Pendente |
| 4 | Agendamento / Recepção | ✅ Concluído |
| 5 | Evolução clínica (SOAP) | ✅ Concluído |
| 6 | Alta e relatórios | ✅ Concluído |

---

## Estrutura do projeto

```
fisioclinic/
├── backend/          # Spring Boot — API REST
├── frontend/         # HTML/CSS/JS — interface
│   ├── pages/        # Páginas por módulo
│   ├── css/          # Estilos
│   └── js/           # Lógica por página e chamadas à API
├── database/
│   ├── setup.sql     # Schema PostgreSQL completo
│   └── setup.ps1     # Script de setup automatizado (Windows)
└── --atualizações/   # Log de alterações do projeto
```

---

## Variáveis de ambiente

O `application.properties` usa valores padrão prontos para desenvolvimento local. Em produção, **substitua todas as variáveis marcadas como obrigatórias** antes de iniciar o backend.

```powershell
# Banco de dados (obrigatórias em produção)
$env:DB_URL      = "jdbc:postgresql://<host>:<porta>/fisioclinic"
$env:DB_USERNAME = "fisio"
$env:DB_PASSWORD = "<senha-segura>"

# JWT (obrigatória em produção — mínimo 32 caracteres aleatórios)
$env:JWT_SECRET     = "<chave-aleatoria-minimo-32-chars>"
$env:JWT_EXPIRATION = "28800000"   # 8 horas em ms (opcional — padrão já definido)

# CORS — origens permitidas separadas por vírgula (obrigatória se o frontend não for localhost)
$env:CORS_ALLOWED_ORIGINS = "https://seu-dominio.com"
```

> **Atenção:** nunca use os valores padrão (`fisio123`, `troque-esta-chave-em-producao`) em produção.
