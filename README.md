# Backend de Hijos de las Ventas

API REST con Spring Boot 4, JPA, MySQL, Spring Security y JWT. Todas las rutas cuelgan de `/api/v1`.

## Arranque local

Necesitas **JDK 17 o 21** (con JDK 23 o superior no compila: Lombok exige configuración adicional; el JDK 25 que trae Android Studio, por ejemplo, falla) y definir `JAVA_HOME` apuntando a él.

También necesitas **MySQL 8** corriendo en `localhost:3306` con el usuario `root` sin contraseña
(ajusta `spring.datasource.*` en `src/main/resources/application.properties` si el tuyo es distinto).
La base `hijosdelasventas_db` se crea sola.

Desde la carpeta `HijosDelasVentasBack`:

```powershell
.\mvnw.cmd spring-boot:run
```

No hace falta configurar variables de entorno en desarrollo:

- El secreto JWT tiene un valor por defecto **solo para desarrollo**. En producción define `JWT_SECRET`
  (Base64 de al menos 32 bytes). Si lo cambias, los tokens emitidos antes dejan de ser válidos.
- La cookie del refresh token es `Secure` por defecto. Si pruebas por HTTP local y quieres probar la
  renovación de sesión, define `JWT_REFRESH_SECURE_COOKIE=false` antes de arrancar.

Al iniciar, `data.sql` carga (sin duplicar al reiniciar) 4 categorías, 6 productos (4 con descuento) y 3 usuarios.

## Usuarios de prueba

| Rol | Correo | Contraseña |
| --- | --- | --- |
| administrador | `admin@duoc.cl` | `admin123` |
| vendedor | `vendedor@duoc.cl` | `vende123` |
| cliente | `carla@gmail.com` | `cliente123` |

## Swagger

Con el backend en ejecución: <http://localhost:8080/swagger-ui.html>.

Swagger lista y documenta todos los endpoints. Para llamar a los protegidos, haz `POST /api/v1/auth/login`,
copia el `accessToken` y envíalo como `Authorization: Bearer <token>` (por ejemplo desde Postman).

## Quién puede qué

| Endpoint | Acceso |
| --- | --- |
| `POST /auth/login`, `/auth/registro`, `/auth/refresh` | público |
| `GET /productos`, `/categorias` y `POST /boletas` (compra) | público |
| `GET /boletas`, `/ordenes` (y por id) | administrador y vendedor |
| `POST/PUT/DELETE /productos`, `/usuarios` | administrador |

La compra toma el usuario del token (sin token queda como invitado) y cobra el precio con descuento
guardado en la base, no el que envía el navegador.

## Tests

```powershell
.\mvnw.cmd test
```

Los tests de integración usan H2 en memoria, no necesitan MySQL.

## Crear usuarios

- **Cliente:** `POST /api/v1/auth/registro` con `run`, `nombre`, `apellidos`, `correo` y `password` (mínimo 8 caracteres).
  Siempre se crea con rol `cliente`. Responde `409` si el RUN o el correo ya existen.
- **Cualquier rol (administrador):** `POST /api/v1/usuarios` con `Authorization: Bearer <token de admin>`, el campo
  `tipo` (`administrador`, `vendedor` o `cliente`) y la `password` en texto normal: el backend la encripta con BCrypt.
