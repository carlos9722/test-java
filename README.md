# API de Gestión de Pólizas

API REST para consultar, renovar y cancelar pólizas y gestionar sus riesgos, con un mock del CORE externo y autenticación por API Key.

## Tecnologías

- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Hibernate, Jakarta Validation)
- Maven (wrapper incluido)
- H2 en memoria
- JUnit 5, Mockito y MockMvc

## Requisitos previos

- JDK 21 o superior (`java -version`)
- No hace falta instalar Maven: se usa `mvnw`

## Instalación y ejecución

```bash
# Compilar y ejecutar todas las pruebas
./mvnw clean verify          # Windows: .\mvnw.cmd clean verify

# Levantar la aplicación en http://localhost:8080
./mvnw spring-boot:run       # Windows: .\mvnw.cmd spring-boot:run
```

También se puede ejecutar el jar: `java -jar target/polizas-0.0.1-SNAPSHOT.jar`

## Configuración

Archivo `src/main/resources/application.properties`.

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `polizas.ipc` | `0.05` | IPC aplicado en renovaciones (0.05 = 5 %). Debe ser >= 0. |
| `seguridad.api-key` | `${POLIZAS_API_KEY:123456}` | API Key requerida en el header. Se sobrescribe con la variable de entorno `POLIZAS_API_KEY`. |
| `spring.h2.console.enabled` | `true` | Consola H2. **Deshabilitar fuera de desarrollo.** |

La aplicación no arranca si el IPC es negativo o la API Key está vacía.

## API Key

Todos los endpoints (excepto `/h2-console`) exigen el header:

```
x-api-key: 123456
```

Sin header o con valor incorrecto responde `401 Unauthorized`.

## Consola H2

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:polizasdb`
- Usuario: `sa`, contraseña: vacía

## Datos de ejemplo

Al arrancar se cargan estos datos (`data.sql`). La base es en memoria: se reinicia con cada arranque.

| id | Póliza | Tipo | Estado | Riesgos |
|---|---|---|---|---|
| 1 | POL-001 | INDIVIDUAL | ACTIVA | 1 |
| 2 | POL-002 | COLECTIVA | ACTIVA | 3 |
| 3 | POL-003 | INDIVIDUAL | CANCELADA | 1 |
| 4 | POL-004 | COLECTIVA | CANCELADA | 2 |
| 5 | POL-005 | COLECTIVA | RENOVADA | 2 |
| 6 | POL-006 | INDIVIDUAL | ACTIVA | 0 |

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/polizas?tipo=&estado=` | Lista pólizas; filtros opcionales e independientes | 200, 400 |
| GET | `/polizas/{id}/riesgos` | Riesgos de una póliza | 200, 404 |
| POST | `/polizas/{id}/renovar` | Aplica el IPC a canon y prima; estado RENOVADA | 200, 400, 404 |
| POST | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos | 200, 400, 404 |
| POST | `/polizas/{id}/riesgos` | Agrega un riesgo (solo colectivas activas) | 201, 400, 404 |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo | 200, 400, 404 |
| POST | `/core-mock/evento` | Simula el envío de un evento al CORE (solo log) | 204, 400 |

Valores válidos: `tipo` = `INDIVIDUAL` | `COLECTIVA`; `estado` = `ACTIVA` | `RENOVADA` | `CANCELADA`.

### Formato de error (todos los errores)

```json
{
  "timestamp": "2026-11-16T18:11:12.74",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Error de validación en los datos enviados",
  "detalles": ["descripcion: La descripción es obligatoria"]
}
```

## Ejemplos con curl

### Linux / macOS / Git Bash

```bash
curl -i -H "x-api-key: 123456" http://localhost:8080/polizas
curl -i -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"
curl -i -H "x-api-key: 123456" http://localhost:8080/polizas/2/riesgos
curl -i -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar
curl -i -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/2/cancelar
curl -i -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
     -d '{"descripcion":"Terremoto"}' http://localhost:8080/polizas/2/riesgos
curl -i -X POST -H "x-api-key: 123456" http://localhost:8080/riesgos/2/cancelar
curl -i -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
     -d '{"evento":"ACTUALIZACION","polizaId":555}' http://localhost:8080/core-mock/evento
```

### Windows PowerShell

Usa `curl.exe` (en PowerShell `curl` es un alias de otro comando) y envía los cuerpos JSON desde archivos, porque el escape de comillas en línea falla:

```powershell
Set-Content -Path riesgo.json -Value '{"descripcion":"Terremoto"}' -Encoding ascii
Set-Content -Path evento.json -Value '{"evento":"ACTUALIZACION","polizaId":555}' -Encoding ascii

curl.exe -i -H "x-api-key: 123456" http://localhost:8080/polizas
curl.exe -i -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar
curl.exe -i -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" -d "@riesgo.json" http://localhost:8080/polizas/2/riesgos
curl.exe -i -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" -d "@evento.json" http://localhost:8080/core-mock/evento
```

## Reglas de negocio

- Una póliza **cancelada no se puede renovar** (400).
- **Renovar** aplica `nuevoValor = valorActual * (1 + IPC)` a canon y prima con `BigDecimal`, redondeo `HALF_UP` a 2 decimales. Una póliza `RENOVADA` puede renovarse de nuevo (el valor se compone).
- **Cancelar una póliza** cancela todos sus riesgos en una sola transacción. Cancelar una ya cancelada devuelve 400.
- **Agregar riesgos**: solo pólizas COLECTIVAS y no canceladas. Las INDIVIDUALES tienen un único riesgo, definido al crear la póliza, y no admiten agregar más (400).
- **Cancelar un riesgo** no cancela la póliza. Cancelar uno ya cancelado devuelve 400.
- **Mock CORE**: `renovar`, `cancelar`, `agregar riesgo` y `cancelar riesgo` envían `ACTUALIZACION` (solo se registra en el log, sin conexiones externas) y solo si la operación fue válida. Las consultas no notifican.

## Pruebas

```bash
./mvnw clean verify
```

- Unitarias (Mockito): servicios, entidad, filtro de API Key, servicio CORE.
- Repositorio y configuración: consultas con filtros, datos semilla, lectura del IPC.
- Integración (MockMvc): los 6 endpoints, el mock CORE, el 401, y los 400/404, de filtro a base de datos.

## Arquitectura

```
com.empresa.polizas
├── controller   Solo HTTP: reciben, validan (@Valid) y delegan
├── service      Reglas de negocio y transacciones (@Transactional)
├── repository   Spring Data JPA
├── entity       Poliza, Riesgo (con comportamiento: cancelar, renovar)
├── dto          Records de entrada y salida (las entidades no se exponen)
├── enums        TipoPoliza, EstadoPoliza, EstadoRiesgo, EventoCore
├── exception    Excepciones de negocio y @RestControllerAdvice
├── security     ApiKeyFilter (filtro de servlet)
└── config       Propiedades externalizadas (IPC, API Key)
```

Decisiones: sin interfaces `Service/Impl` (una sola implementación), DTOs como `record`, filtros opcionales con una consulta JPQL, `open-in-view=false`, y la API Key en un filtro de servlet en lugar de Spring Security.

## Limitaciones conocidas

- El log del mock CORE se emite dentro de la transacción. En producción se usaría `@TransactionalEventListener(AFTER_COMMIT)` o un outbox.
- No hay endpoint para crear pólizas: se usan datos semilla.
- Los filtros con `is null or` en JPQL funcionan en H2. Con más filtros o en otra BD conviene `Specification`.