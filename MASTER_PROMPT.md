# Master Prompt — Admin Club (MiClub)

Documento técnico para que cualquier IA replique la aplicación **Admin Club** desde cero: arquitectura, entidades, flujos, convenciones y criterios de aceptación.

---

## PROMPT MAESTRO

```
Eres un arquitecto y desarrollador senior. Debes construir desde cero una aplicación web llamada **Admin Club** (MiClub): plataforma multi-club para administración deportiva con cuotas mensuales, pagos, finanzas, asistencia y comunicaciones.

Stack obligatorio:
- Java 17, Spring Boot 3.5.x
- Spring MVC + Thymeleaf (server-side rendering, NO SPA)
- Spring Security con @Secured (method security)
- Spring Data JPA + MySQL 8
- Jakarta Bean Validation
- JavaMail + plantillas Thymeleaf HTML para correos
- Bootstrap 5.2, Font Awesome 5, jQuery 3.3, SweetAlert2
- OpenPDF (reportes PDF), Apache POI (Excel)
- Integración Khipu (pagos online Chile)
- @EnableAsync + @EnableScheduling

Arquitectura en capas clásica Spring (NO microservicios):
Controller → Service (interface I* + impl *Impl) → Repository (I*Repository) → Entity JPA
```

---

## 1. Arquitectura de archivos

```
admin-club/
├── pom.xml
├── src/main/java/com/app/
│   ├── AdminClubApplication.java          # @SpringBootApplication, @EnableAsync, @EnableScheduling
│   ├── SpringSecurityConfig.java          # SecurityFilterChain, BCrypt, form login
│   ├── MvcConfig.java                     # RestTemplate, error_403 view
│   ├── auth/
│   │   ├── LoginSuccessHandler.java       # Multi-club post-login
│   │   └── LoginFailureHandler.java
│   ├── config/
│   │   ├── AsyncConfig.java               # Pool emailTaskExecutor
│   │   └── CorrelationIdFilter.java       # MDC correlationId en logs
│   ├── security/AppRoles.java             # Constantes ROLE_*
│   ├── controllers/                         # ~17 controllers MVC + GlobalExceptionHandler
│   ├── dto/                                 # DTOs y Forms (*DTO, *Form)
│   ├── entity/                              # Entidades JPA core
│   ├── enums/                               # EstadoPago, MedioPago, ConceptoPago
│   ├── repository/                          # I*Repository extends JpaRepository
│   ├── service/                             # I*Service + *ServiceImpl
│   ├── notification/                        # Submódulo notificaciones
│   │   ├── domain/                          # NotificationConfig, NotificationSendLog
│   │   ├── dto/, repository/, service/, support/, web/
│   ├── khipu/                               # Verificación HMAC webhook
│   ├── mail/                                # Logo inline en emails
│   ├── helper/                              # KhipuCreatePaymentHelper
│   └── util/AfterCommitRunner.java          # Emails post-commit
├── src/main/resources/
│   ├── application.properties
│   ├── import.sql                           # Seed Hibernate post-DDL
│   ├── db/01-schema-adminclub.sql           # DDL referencia MySQL
│   ├── templates/
│   │   ├── layout/layout.html               # Layout maestro (head, header, footer)
│   │   ├── email/                           # Plantillas correo HTML
│   │   └── [vistas por módulo].html
│   └── static/{css,js,images}/
└── src/test/java/com/app/                   # Tests integración + ExportImportSqlTool
```

**Principio multi-tenant:** un mismo email puede existir en varios clubes (`UNIQUE(email, id_club)`). El contexto operativo es `idClubSession` en sesión HTTP, no solo el principal de Spring Security.

---

## 2. Entidades de datos y modelos

### Modelo de dominio central

| Entidad | Tabla | Descripción |
|---------|-------|-------------|
| **Club** | `club` | Tenant. `nombre`, `codigo` (unique), `tipo`, `estado` (1=activo), `diaVencimientoCuota`, `logo` (BLOB) |
| **Usuario** | `usuarios` | Usuario por club. `email`, `password` (BCrypt), `nombre`, `enabled`, `estado` (0=clave temporal) |
| **Role** | `usuarios_rol` | `authority` = ROLE_* |
| **Deportista** | `deportistas` | Atleta vinculado a apoderado (`Usuario`) y `Categoria`. `fechaIngreso` define desde cuándo se cobra |
| **Categoria** | `categorias` | Grupo etario con `valorCuota` base |
| **CategoriaValorVigencia** | `categoria_valor_vigencia` | Valor cuota por mes/año. Unique (categoria, anio, mes) |
| **Pago** | `pago` | Cuota o cobro. `mes`, `anio`, `monto`, `concepto`, `estado`, `medioPago`, comprobante transferencia (BLOB) |
| **OrdenPago** | `orden_pago` | Agrupa pagos Khipu. `khipuPaymentId`, `khipuUrl`, `montoTotal` |
| **CuentaBancaria** | `cuentas_bancarias` | 1:1 con Club. Datos transferencia + credenciales Khipu por club |
| **Banco** | `bancos` | Catálogo bancos Chile |
| **NoPagoConfig** | `no_pago_config` | Meses sin cobro. Scope: CLUB, CATEGORIA, DEPORTISTA |
| **AsistenciaClase** | `asistencia_clase` | Unique (club, deportista, fechaClase). `presente`, entrenador |
| **Temporada** | `temporadas` | Temporadas deportivas (opcional en pagos) |
| **ClubHistorialCambio** | `club_historial_cambio` | Auditoría cambios club |
| **EmailEnvio** | `email_envios` | Log campañas correo masivo |

### Submódulo notificaciones

| Entidad | Tabla | Descripción |
|---------|-------|-------------|
| **NotificationConfig** | `notification_config` | Por club y tipo: enabled + daysOffset |
| **NotificationSendLog** | `notification_send_log` | Dedupe envíos (club, deportista, tipo, mes, anio) |

### Enums críticos

```java
EstadoPago: PENDIENTE, PENDIENTE_KHIPU, RECHAZADO, PAGADO, MOROSO
MedioPago: KHIPU, EFECTIVO, TRANSFERENCIA, WEBPAY
ConceptoPago: MENSUALIDAD, INSCRIPCION, MATRICULA, IMPLEMENTACION, OTRO
NotificationType: BEFORE_DUE, AFTER_DUE, PAYMENT_RECEIVED
NoPagoConfig.Scope: CLUB, CATEGORIA, DEPORTISTA
```

### DTOs / Forms principales

- `MesPagoDTO` — cuota pendiente (deportista, mes, año, monto, conceptoLabel, seleccionKey)
- `DashboardPagoDTO`, `MorosidadClubDTO`, `EstadoPagoDeportistaDTO`
- `CobroAdicionalForm`, `NoPagoConfigForm`, `TipoAltaUsuarioClub` (APODERADO, TESORERO, ENTRENADOR)
- `FinancieroDashboardDTO`, `AsistenciaEstadisticaDTO`
- `MassEmailRequest`, `MassEmailFilter` (ALL, CATEGORY, CUSTOM, DEBTORS)

### Relaciones clave

```
Club 1──1 CuentaBancaria
Club 1──N Categoria, Usuario
Club 1──N NoPagoConfig, NotificationConfig
Usuario 1──N Deportista, Role
Deportista N──1 Categoria, Usuario (apoderado)
Pago N──1 Deportista, Club, OrdenPago?
OrdenPago 1──N Pago
Categoria 1──N CategoriaValorVigencia
```

---

## 3. Roles y seguridad

### Roles (`AppRoles`)

| Rol | Alcance |
|-----|---------|
| **ROLE_ADMIN** | Plataforma: CRUD clubes, incidencias pagos, modo soporte opcional |
| **ROLE_CLUB** | Admin completo del club |
| **ROLE_TESORERO** | Finanzas, pagos, comunicaciones (sin eliminar usuarios en algunas rutas) |
| **ROLE_ENTRENADOR** | Deportistas + asistencia. **Sin** pagos, finanzas, cuentas, correos |
| **ROLE_USER / ROLE_SOCIO** | Apoderado: consulta/pago cuotas, perfil, asistencia propia |

Arrays reutilizables: `CLUB_STAFF`, `CLUB_FINANZAS`, `APODERADO`.

### Sesión HTTP

| Atributo | Uso |
|----------|-----|
| `usuarioLogin` | Usuario del club activo |
| `idClubSession` | Long — tenant activo |
| `rolesActivos` | Roles del login |
| `usuariosClub` | Lista para selector multi-club |
| `adminSoporte` | Modo soporte admin (solo lectura parcial) |

### Flujo login

1. Form login: `email` + `password` → BCrypt.
2. `JpaUserDetailsService`: une roles de **todos** los `Usuario` con mismo email.
3. `LoginSuccessHandler`:
   - 0 clubes activos → `/sinClub`
   - 1 club → setea sesión → redirect por rol
   - N clubes → `/seleccionarClub`
4. Redirects: ADMIN→`/listadoClub`; clave temporal→`/actualizarPass`; staff→`/listadoDeportistas`; apoderado→`/consulta`.

**Regla:** todo controller de club debe validar `idClubSession != null` y usar `usuarioService.refrescarUsuarioSesion()`.

---

## 4. Módulos funcionales (implementar todos)

### 4.1 Pagos y cuotas

- **Generación cuotas:** recorrer meses desde `fechaIngreso` del deportista hasta mes actual + 6; excluir meses con pago bloqueante y meses `NoPagoConfig`.
- **Valor cuota:** `ICategoriaCuotaVigenciaService.obtenerValorCuota(categoriaId, anio, mes)` con fallback a `Categoria.valorCuota`.
- **Medios de pago apoderado:** efectivo siempre; transferencia si cuenta bancaria completa; Khipu si api key configurada (`ClubMediosPagoService`).
- **Efectivo:** crea `Pago` estado `PENDIENTE` → club aprueba/rechaza.
- **Transferencia:** adjunta comprobante (imagen/PDF, max 5MB) → `PENDIENTE`.
- **Khipu:** crea `OrdenPago` + pagos `PENDIENTE_KHIPU` → redirect URL → webhook `POST /api/khipu/notify` (HMAC, idempotente) → `PAGADO`.
- **Morosidad:** deportista sin pago bloqueante en mes/año; excluir pre-ingreso y no-pago.
- **Cobros adicionales:** MATRICULA, IMPLEMENTACION, OTRO; alcance deportista/categoría/club; aparecen en consulta apoderado con `seleccionKey=ADICIONAL-{id}`.

### 4.2 Categorías con vigencia histórica

- Al editar categoría, registrar vigencias mes/año con combobox de meses.
- Diciembre 2025 y enero 2026 pueden tener valores distintos; morosos pagan valor vigente del período adeudado.

### 4.3 Meses sin cobro (No Pago)

- Configurable por club, categoría o deportista para mes/año específico.
- Integrar en: meses para pagar, morosidad, estado mensual club, recordatorios automáticos, filtro DEBTORS en correo masivo.

### 4.4 Financiero

- Dashboard ingresos, morosidad, conciliación Khipu.
- Export PDF (OpenPDF) y Excel (POI).

### 4.5 Notificaciones

- **Automáticas:** cron diario (ej. 08:00) → recordatorio BEFORE_DUE y AFTER_DUE según `Club.diaVencimientoCuota` + offset configurable; dedupe en `NotificationSendLog`.
- **PAYMENT_RECEIVED:** aviso al club al acreditar pago (respetar flag enabled).
- **Masivo:** filtros ALL/CATEGORY/CUSTOM/DEBTORS; envío async un email por destinatario único.

### 4.6 Asistencia

- Entrenador registra presente/ausente por deportista y fecha.
- Estadísticas: staff ve club; apoderado solo sus deportistas.
- Unique constraint (club, deportista, fechaClase).

### 4.7 Gestión usuarios club

- ROLE_CLUB crea: apoderado (ROLE_USER), tesorero (ROLE_TESORERO), entrenador (ROLE_ENTRENADOR).
- TESORERO/ENTRENADOR no crean staff.
- Formulario condicional según `TipoAltaUsuarioClub`.

### 4.8 Panel admin plataforma

- CRUD clubes + usuario ROLE_CLUB inicial.
- Incidencias: pagos problemáticos + órdenes Khipu colgadas.
- Modo soporte (`admin.soporte.enabled`): admin navega como club.

---

## 5. Flujos de usuario principales

### Apoderado (ROLE_USER)

```
Login → [Seleccionar club] → /consulta
  → Ver cuotas pendientes + cobros adicionales
  → Seleccionar meses → Pagar (efectivo | transferencia + comprobante | Khipu)
  → Ver historial pagos
  → /perfilUsuario (datos + deportistas)
  → /asistencia/estadisticas (solo sus hijos)
```

### Club / Tesorero

```
Login → /listadoDeportistas
  → CRUD deportistas, apoderados, tesoreros, entrenadores
  → /listadoPagos → aprobar/rechazar efectivo y transferencias
  → /categorias → valores y vigencias
  → /cuentas → cuenta bancaria + Khipu
  → /cobros-adicionales
  → /no-pago → meses sin cobro
  → /financiero → reportes + export
  → Comunicaciones → config automática + correo masivo
  → /perfilClub
```

### Entrenador

```
Login → /listadoDeportistas
  → /asistencia/registro (marcar asistencia)
  → /asistencia/estadisticas
  (sin acceso a pagos, finanzas, cuentas, comunicaciones)
```

### Admin plataforma

```
Login → /listadoClub
  → Crear/editar clubes
  → /admin/incidencias-pagos
  → /admin/club/{id} detalle
  → [Opcional] modo soporte
```

### Khipu (técnico)

```
Apoderado POST /pagar (Khipu)
  → OrdenPago + pagos PENDIENTE_KHIPU
  → Redirect a khipuUrl
  → Usuario paga → Khipu POST /api/khipu/notify
  → Verificar firma HMAC → confirmarPagoKhipu → PAGADO
  → Email async club + apoderado (AfterCommitRunner)
  → Browser retorno /pago/khipu/retorno
```

---

## 6. Controladores y rutas (mapa mínimo)

| Ruta | Roles | Propósito |
|------|-------|-----------|
| `/login`, `/recuperacion` | Público | Auth |
| `/seleccionarClub`, `/setClubActivo` | Auth | Multi-club |
| `/consulta`, `/pagar` | USER, SOCIO | Cuotas apoderado |
| `/listadoPagos`, `/aprobar`, `/rechazar` | CLUB, TESORERO | Gestión pagos |
| `/categorias/*` | CLUB, TESORERO | Categorías |
| `/cuentas/*` | CLUB, TESORERO | Medios pago |
| `/financiero/*` | CLUB, TESORERO | Reportes |
| `/cobros-adicionales` | CLUB, TESORERO | Cobros extra |
| `/no-pago` | CLUB, TESORERO | Meses sin cobro |
| `/notificaciones/*` | CLUB, TESORERO | Comunicaciones |
| `/asistencia/*` | Staff + apoderado | Asistencia |
| `/listadoClub`, `/admin/*` | ADMIN | Plataforma |
| `POST /api/khipu/notify` | Público (CSRF off) | Webhook |
| `/api/notifications/*` | CLUB, TESORERO | REST notificaciones |

---

## 7. Estilo de codificación y convenciones

### Naming

- Repositorios: `I{Entity}Repository`
- Servicios: `I{Domain}Service` / `{Domain}ServiceImpl`
- Controllers: `{Domain}Controller`
- DTO lectura: `*DTO`; forms MVC: `*Form`

### Patrones obligatorios

- `@Secured` en controllers (no `@PreAuthorize` salvo excepción).
- `@Transactional` en servicios; `readOnly=true` en consultas.
- Validación Jakarta en forms (`@Valid`, `@NotBlank`, `@Min`, `@Max`).
- Validación negocio: `IllegalArgumentException` → `GlobalExceptionHandler`.
- **Emails:** controllers llaman `AsyncEmailService`, nunca `IEmailService` directo en HTTP.
- **Post-commit:** `AfterCommitRunner.run(() -> asyncEmailService...)` tras persistir pagos.
- **Multi-club:** `resolveUsuarioActivo(email, idClubSession)` en toda operación.
- **Mensajes UI:** flash `msjLayout` formato `"tipo;título;mensaje"` → SweetAlert2 en `general.js`.
- **CSRF:** token en todos los forms POST Thymeleaf.
- **Estado entidades:** String `"1"` activo, `"0"` inactivo/temporal.

### Frontend

- Layout fragmentado: `th:replace="~{layout/layout :: head|header|footer}"`.
- Navbar condicional: `sec:authorize="hasAnyRole('ROLE_CLUB','ROLE_TESORERO')"`.
- Bootstrap 5.2 cards, rounded-pill buttons, Font Awesome icons.
- JS por módulo: `pago.js`, `gestionPagos.js`, `usuario.js`, `categoria.js`, etc.

### Base de datos

- Dev: `spring.jpa.hibernate.ddl-auto=create-drop` + `import.sql` seed.
- Prod: migrar a `validate` + Flyway/Liquibase.
- DDL referencia en `db/01-schema-adminclub.sql`.
- Timezone: `America/Santiago`.

### Configuración clave (`application.properties`)

```properties
server.port=8081
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:mysql://localhost/bd_adm_club?serverTimezone=America/Santiago
app.public.url=http://localhost:8081
notifications.scheduler.enabled=true
notifications.scheduler.cron=0 0 8 * * ?
khipu.webhook.verify-signature=true
admin.soporte.enabled=false
```

---

## 8. Librerías Maven (pom.xml)

| Dependencia | Uso |
|-------------|-----|
| spring-boot-starter-web | MVC |
| spring-boot-starter-data-jpa | ORM |
| spring-boot-starter-security | Auth |
| spring-boot-starter-validation | Bean Validation |
| spring-boot-starter-mail | SMTP |
| thymeleaf-extras-springsecurity6 | sec:authorize en vistas |
| mysql-connector-j | MySQL |
| lombok 1.18.x | Boilerplate (opcional) |
| openpdf 1.3.x | PDF financiero |
| poi-ooxml 5.2.x | Excel financiero |
| h2 | Tests |
| jacoco | Cobertura |

---

## 9. Criterios de aceptación (replicación completa)

1. Multi-club: mismo email en N clubes con selector post-login.
2. Cuotas con vigencia histórica por categoría mes/año.
3. Tres medios de pago: efectivo (aprobación manual), transferencia (comprobante), Khipu (webhook).
4. Morosidad excluye meses No Pago y pre-ingreso deportista.
5. Cobros adicionales integrados en consulta apoderado.
6. Notificaciones automáticas configurables + correo masivo con filtro morosos.
7. Asistencia con estadísticas diferenciadas staff vs apoderado.
8. Roles granulares: CLUB, TESORERO, ENTRENADOR, USER, ADMIN.
9. Panel admin plataforma independiente del club.
10. Emails HTML con plantillas Thymeleaf y envío asíncrono post-commit.

---

## 10. Orden de implementación sugerido

```
Fase 1: Club, Usuario, Role, Security, login multi-club, layout Thymeleaf
Fase 2: Categoria, Deportista, CategoriaValorVigencia, listados club
Fase 3: Pago, cuotas pendientes, consulta apoderado, aprobación efectivo
Fase 4: Transferencia + comprobante, Khipu + webhook
Fase 5: Financiero, dashboard, exports PDF/Excel
Fase 6: NoPagoConfig, cobros adicionales
Fase 7: Notificaciones (scheduler + masivo + REST)
Fase 8: Asistencia, entrenador, estadísticas
Fase 9: Admin plataforma, incidencias, modo soporte
Fase 10: Tests integración Khipu, seguridad pagos, seed import.sql
```

---

## Uso recomendado

1. Copiar el bloque **PROMPT MAESTRO** (sección inicial) como system prompt o primer mensaje en la IA.
2. Implementar **fase por fase** (sección 10) para evitar respuestas incompletas.
3. En producción, cambiar `ddl-auto=create-drop` por migraciones versionadas.

---

*Generado a partir del código fuente de Admin Club v1 — Spring Boot 3.5.6, Java 17.*
