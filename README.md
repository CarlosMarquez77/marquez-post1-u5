# Post-contenido — Unidad 5: Integración en Aplicaciones Web

## Descripción
Repositorio del post-contenido de la Unidad 5 de Patrones de Diseño
de Software. Un único proyecto Spring Boot (reservas-labs-api) para
la reserva de laboratorios de cómputo, con dos partes: una API REST
en capas (Entity, Repository, Service, Controller) sobre H2, y una
vista Thymeleaf (MVC clásico) que reutiliza el mismo Service.

## Parte 1 — Repository, Service y Controller REST
LaboratorioRepository y ReservaRepository extienden JpaRepository;
ReservaRepository agrega una consulta JPQL propia para detectar
solapamientos de horario. ReservaService concentra las reglas de
negocio (solapamiento, horario de atención, duración, cancelación
tardía). ReservaController y LaboratorioController exponen
/api/reservas y /api/laboratorios. Ver paquetes model/, repository/,
service/, exception/ y controller/.

## Cómo ejecutar
```
$ mvn clean package
$ mvn spring-boot:run
```
API REST: http://localhost:8080/api/reservas

## Decisiones de diseño

### Punto de decisión 1 — Ubicación de la validación de solapamiento
La búsqueda de las reservas que se cruzan la hace el Repository con la
consulta JPQL `buscarSolapamientos`. Así el filtro lo resuelve la base
de datos y no hay que traer a memoria todas las reservas del
laboratorio, que van creciendo con el tiempo. La decisión de rechazar
la reserva la toma solo `ReservaService.crear`: si la consulta devuelve
alguna reserva, lanza `ReservaConflictException` con el mensaje para el
usuario. El Repository responde qué reservas se solapan y el Service
decide si se permite crear la reserva.

La alternativa era traer todas las reservas con `findByLaboratorioId` y
comparar los rangos en Java. Se descartó porque cada reserva nueva
tardaría más a medida que el laboratorio acumula historial.

Si el Controller llamara directamente a `buscarSolapamientos()`, la
regla quedaría en la capa HTTP y habría que repetirla en cada
controlador que cree reservas (por ejemplo el controlador MVC de la
Parte 2), con el riesgo de que uno valide y otro no.

### Punto de decisión 2 — Reglas con y sin apoyo del Repository
El horario de atención (07:00 a 21:00) y la duración (30 minutos a 3
horas) solo dependen del inicio y el fin de la reserva que se está
creando, no de otras filas de la base de datos. Por eso
`validarHorarioYDuracion` está completa en el Service, en Java, y no
usa el Repository. El criterio fue: si la regla necesita compararse con
datos que solo conoce la base de datos (las otras reservas), se apoya
en una consulta del Repository; si solo depende del propio objeto, se
queda en el Service.

Para estas reglas se lanza `ReservaInvalidaException`, que se responde
con 400 porque el error está en los datos enviados. El solapamiento se
responde con 409 porque es un conflicto con una reserva que ya existe.

`LaboratorioController` es la única excepción a que el Controller no
use el Repository: el catálogo de laboratorios es un CRUD sin reglas de
negocio, y un `LaboratorioService` que solo delegara sería un Service
anémico. La capa Service se agrega cuando hay una regla que la
justifique.

## Herramientas utilizadas
- Java 17, Spring Boot 3.2, Spring Data JPA, H2
- Apache Maven, Postman/curl, Git, GitHub
