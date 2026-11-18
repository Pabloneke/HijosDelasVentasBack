# Backend de Hijos de las Ventas

## Arranque local con PowerShell

El backend requiere `JWT_SECRET` para arrancar. El valor debe estar codificado en Base64 y contener al menos 32 bytes al decodificarlo.

Con MySQL disponible, abre PowerShell en la carpeta `HijosDelasVentasBack` y ejecuta:

```powershell
$jwtKey = New-Object byte[] 32
$generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$generator.GetBytes($jwtKey)
$generator.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($jwtKey)
$env:JWT_REFRESH_SECURE_COOKIE = "false"

.\mvnw.cmd spring-boot:run
```

Usa `JWT_REFRESH_SECURE_COOKIE=false` solo al probar la renovación de sesión por HTTP local. Para HTTPS, conserva el valor predeterminado `true`.

Las variables se aplican a la sesión actual de PowerShell y a los procesos que inicia. Si generas otra clave, los tokens emitidos con la anterior dejarán de ser válidos. Guarda la clave fuera del repositorio.

## Crear un usuario desde Postman

### Registrar un cliente

1. Con el backend en ejecución, crea una solicitud `POST` a `http://localhost:8080/api/v1/auth/registro`.
2. En **Authorization**, selecciona **No Auth**. En **Body**, selecciona **raw → JSON**. Postman enviará `Content-Type: application/json`.
3. Usa un RUN y un correo que aún no estén registrados, y una contraseña de al menos 8 caracteres.

```json
{
  "run": "123456785",
  "nombre": "Ana",
  "apellidos": "Pérez",
  "correo": "ana@example.com",
  "password": "Prueba123!"
}
```

Al enviar la solicitud, el backend devuelve `201 Created` con `accessToken` y el perfil en `usuario`. El registro asigna el rol `cliente` y guarda la contraseña con BCrypt. Si el RUN o el correo ya existe, devuelve `409 Conflict`.

### Crear un usuario como administrador

1. Envía un `POST` a `http://localhost:8080/api/v1/auth/login` con el `correo` y la `password` de una cuenta administradora en **Body → raw → JSON**. Copia el `accessToken` de la respuesta.
2. Crea una solicitud `POST` a `http://localhost:8080/api/v1/usuarios`. En **Authorization**, selecciona **Bearer Token** y pega el token.
3. En **Body → raw → JSON**, usa la misma estructura del ejemplo, con un RUN y un correo nuevos. Agrega `"tipo": "vendedor"` o el rol que corresponda (`administrador`, `vendedor` o `cliente`). En `password` envía un hash BCrypt ya generado, porque este endpoint guarda el valor recibido directamente.
4. Envía la solicitud y comprueba que la respuesta sea `201 Created`.
