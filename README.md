## ECOMMERCE BATCH
___


### DEPENDENCIAS:
- Lombok
- Spring Web
- Java Mail Sender
- MySQL Driver
- Docker
- Spring Batch
- Spring Boot 3.2.1

### DESCRIPCCIÓN:

Las funcionalidades de este proyecto son:
- Realizar la insercción de productos recibidos de un archivo CSV a una base de datos desplegada con Docker
- Crear un archivo CSV con los resultados de los productos DTO insertados
- Enviar un correo con los resultados y el archivo CSV

### TODO:
- Generar PDF de los resultados obtenidos añadir al mail
- Añadir cabecera de campos en primera linea del CSV enviado por correo
- Hacerlo tolerante a fallos
- Añadir conexión con kibana

