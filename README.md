# Run API
.\mvnw.cmd spring-boot:run -DskipTests


# Setting up H2 database
https://www.baeldung.com/spring-boot-h2-database  
https://www.baeldung.com/h2-embedded-db-data-storage  


# H2 Web gui
http://localhost:9093/


# Java Bean validation
https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html


# TODO's:
- Review @Transactional annotations
- Create Mock tests


# JPA derived query method names
For nested properties in Spring Data JPA you must include And between predicates, and use underscores (_) to traverse associations.


# Define customerized JPA queries  
Use JPQL


# Set rollback to false so the SpringBootTests are not automatically rolled back 
@Rollback(false)


# Swagger URL
http://localhost:8080/swagger-ui/index.html


# Theamleaf URL
http://localhost:8080/ui/users


# Spring Boot devtools
https://docs.spring.io/spring-boot/reference/using/devtools.html#using.devtools.livereload


# Swagger / OpenAPI
Main OpenAPI spec: http://localhost:8080/v3/api-docs
Swagger UI: http://localhost:8080/swagger-ui.html


# Generate openapi.yaml
.\mvnw.cmd verify -DskipTests


# Todo:
session vs token???


# table-dragger
https://www.jsdelivr.com/?query=table-dragger
https://github.com/sindu12jun/table-dragger


# Mistral (AI with github.com integration)
https://chat.mistral.ai/code


# Last working version
Revision number: 8785a35c50c1d5be6b23a9461488bf4bd5aab2fe
Commit message: Fix Maximum call stack size exceeded - Added bs-select-initialized data flag to prevent duplicate initialization of selectpicker event listeners


Revision number: 4cd5f92d50f92335f3a098ad5dad65372dbcd1e0
Commit message: Fix HTMX endpoint not being called after drag-and-drop - Added htmx.process() to re-process HTMX attributes after container replacement and moved event listeners to initSelectpicker()
