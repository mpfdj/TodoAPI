# Run API
.\mvnw.cmd spring-boot:run -DskipTests


# Setting up H2 database
https://www.baeldung.com/spring-boot-h2-database  
https://www.baeldung.com/h2-embedded-db-data-storage  


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

