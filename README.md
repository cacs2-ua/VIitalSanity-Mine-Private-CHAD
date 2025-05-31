# 💟 VitalSanity: La Aplicación Web donde el Paciente es el Protagonista

# 🌐 URL de producción de **VitalSanity**

### Puedes acceder a la **URL** de producción haciendo clic en el siguiente [**<u>enlace</u>**](https://www.vitalsanity.net:11443/vital-sanity).


Para la puesta en producción se ha utilizado **AWS**, aprovechando las tecnologías y herramientas mencionadas en la memoria.
Además de las tecnologías que se explican en la memoria, también se ha empleado **Amazon RDS** para la configuración de la base de datos **PostgreSQL** en producción y **AWS Elastic Beanstalk** para desplegar la aplicación de forma **sencilla** y **escalable**.

Destacar que el objetivo principal de la puesta en producción ha sido, por un lado, el ir un paso más en el **ciclo del desarrollo de Software** para **VitalSanity** y, por otro lado, el facilitar la comprobación de las funcionalidades de la aplicación a cualquier persona que desee acceder a ella. Asimismo, mencionar que la [**<u>URL</u>**](https://www.vitalsanity.net:11443/vital-sanity) de producción se utilizará el día de la **defensa** del **TFG** para poder mostrar las funcionalidades de la aplicación de forma **sencilla** y **natural**.

## 🍃 Datos para probar la aplicación

Para probar la aplicación, se han creado una serie de **usuarios de prueba** junto con otros datos de ejemplo (como documentos, informes médicos, solicitudes de autorización, ...).
Todo esto se ha realizado con el objetivo de facilitar todo lo posible la correcta comprobación de las funcionalidades de la aplicación.

Estos son los usuarios de prueba (la contraseña para todos ellos es **vitalsanity123** y se puede iniciar sesión con **email** y **contraseña**):

- **admin@gmail.com** (Administrador)
- **quiron-puertollano@example.es** (Centro médico)
- **adeslas-alicante@example.es** (Centro médico)
- **sergio-castillo-blanco@gmail.com** (Paciente)
- **carmen-ruiz-herrera@gmail.com** (Paciente)
- **cacs2@alu.ua.es** (Profesional médico y el creador de VitalSanity jeje)
- **juan.perez@example.com** (Profesional médico)
- **maria.lopez@example.com** (Profesional médico)
- **carlos.garcia@example.com** (Profesional médico)
- **monica.garcia@example.com** (Profesional médico)
- **manuel.gimenez@example.com** (Profesional médico)
- **laura.hernandez@example.com** (Profesional médico)
- **pablo.rodriguez@example.com** (Profesional médico)

En caso de querer probar la aplicación en local, se pueden insertar manualmente todos estos datos de prueba ejecutando el fichero de **seeders** **_resources/sql/database-script/seed_develop_db.sql_** (esto se explica a continuación).

# 🛠️ Instrucciones para ejecutar el proyecto en local

Cabe destacar que es altamente recomendable probar la aplicación **directamente** desde la [**<u>URL</u>**](https://www.vitalsanity.net:11443/vital-sanity) de **producción**.
Aparte de que es mucho más cómodo, en el código del proyecto se han ocultado (por razones obvias) todas aquellas **credenciales sensibles** (como las credenciales de **AWS**).

Por esta razón, si se quisiera probar la aplicación en local en su totalidad, se debería de modificar manualmente los ficheros de configuración (los ficheros **_.properties_**) para introducir **credenciales propias**.

Aun así, mencionar que se puede probar en local (y sin introducir ningunas credenciales de forma manual) todas las funcionalidades (incluyendo las que involucran a **Autofirma** y/o **Cliente móvil @firma**)
salvo visualizar la recepción de correos en **Mailtrap** y aquellas que involucren subida o descarga de ficheros de **Amazon S3**.

Hechas estas aclaraciones, a continuación se explican las instrucciones para ejecutar el proyecto en local.

## 🔥OPCIÓN 1: Levantar el Proyecto usando Docker Compose

Gracias la configuración establecida con **Docker Compose**, la aplicación puede ejecutarse
desde cualquier ordenador mediante los siguientes comandos:

```sh
./mvnw clean package
```

```sh
docker compose up
```

El único requisito necesario es tener instalado **Docker**. Esta es la opción más cómoda y rápida. Asimismo, con esta opción se ejecuta automáticamente el fichero de **seeders**.

## 🔥OPCIÓN 2: Ejecutar la aplicación cargando el perfil de Postgres

En este caso, es necesario tener instalado lo siguiente:

- **JDK 21**
- **Docker (cualquier versión moderna)**

Teniendo esos requisitos instalados, la aplicación también puede ser ejecutada mediante los siguientes comandos:

En primer lugar, ejecutar este comando para levantar la base de datos de ‘**PostgreSQL**‘
en un contenedor de ‘**Docker**‘.

```sh
docker run --name postgres-vitalsanity-develop -e POSTGRES_USER=vital -e POSTGRES_PASSWORD=vital -e POSTGRES_DB=vital -p 5058:5432 -d postgres:13
```

A continuación, ejecutar el siguiente comando para arrancar la aplicación cargando el
perfil de **postgres**:

```sh
mvn spring-boot:run -D spring-boot.run.profiles=postgres
```

Por último, en este caso es necesario ejecutar manualmente el fichero de seeders **_resources/sql/database-script/seed_develop_db.sql_**

En efecto, observamos como es mucho más cómodo y rápido usar directamente la [**<u>URL</u>**](https://www.vitalsanity.net:11443/vital-sanity) de **producción**.



