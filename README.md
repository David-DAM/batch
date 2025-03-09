# Proyecto de Spring Batch

Este proyecto implementa un flujo de procesamiento por lotes utilizando Spring Batch. Incluye dependencias de JPA, Web,
JasperReports, Spring Mail, Lombok y MySQL. Además, cuenta con servicios de Docker configurados con el ELK stack para
monitoreo y una base de datos MySQL.

## Tabla de Contenidos

- [Requisitos Previos](#requisitos-previos)
- [Flujo de la Aplicación](#flujo-de-la-aplicación)
- [Características Principales](#características-principales)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Docker](#docker)
- [Dependencias](#dependencias)
- [Contribuciones](#contribuciones)

---

## Requisitos Previos

- **Java**: Versión 17 o superior.
- **Maven**: Para construir el proyecto.
- **Docker**: Instalado y configurado.

La aplicación estará disponible en `http://localhost:8080`.

## Flujo de la Aplicación

1. **Entrada**: Se recibe un archivo CSV como entrada.
2. **Procesamiento**:
    - Los datos del CSV se almacenan en la base de datos MySQL.
    - Se utiliza un flujo de decisión (`FlowDecision`) para manejar caminos alternativos.
    - Es tolerante a fallos y utiliza listeners para gestionar errores y eventos.
3. **Salida**:
    - Se genera un archivo PDF y CSV con los resultados del reporte.
    - El reporte se adjunta a un correo electrónico que se envía al destinatario.

## Características Principales

- **Spring Batch**: Gestión de trabajos por lotes con listeners y tolerancia a fallos.
- **JPA**: Para el manejo de entidades y operaciones en la base de datos.
- **JasperReports**: Generación de reportes personalizados.
- **Spring Mail**: Envío de correos electrónicos con los resultados del procesamiento.
- **Docker**:
    - Base de datos MySQL.
    - ELK stack para monitoreo de logs.

## Estructura del Proyecto

- **jobs**: Definiciones de trabajos y flujos.
- **listeners**: Gestión de eventos y errores.
- **processors**: Lógica de procesamiento.
- **readers**: Lectores de datos.
- **writers**: Escritores de datos.

## Docker

### Servicios Incluidos

- **MySQL**: Base de datos para almacenar los datos procesados.
- **ELK Stack**: Para monitoreo y análisis de logs.
    - **Logstash**: Configurado para recibir y procesar logs.
    - **Elasticsearch**: Almacenamiento de logs.
    - **Kibana**: Visualización y análisis.

## Dependencias

Las principales dependencias utilizadas son:

- **Spring Batch**: Para gestionar trabajos por lotes.
- **Spring Data JPA**: Persistencia con MySQL.
- **Spring Web**: Para servicios REST.
- **Spring Mail**: Envío de correos electrónicos.
- **JasperReports**: Generación de reportes.
- **Lombok**: Reducción de código repetitivo.

## Contribuciones

¡Contribuciones son bienvenidas! Si tienes ideas, mejoras o nuevos casos de uso, abre un issue o envía un pull request.