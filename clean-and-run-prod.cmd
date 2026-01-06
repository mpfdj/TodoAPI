:: Set environment variables
set APP_PROFILE=prod
set JWT_SECRET=mySuperSecretKeyThatIsAtLeast32BytesLong123!
set H2_PASSWORD=sa

:: Clean
rmdir /s /q target

:: Run
.\mvnw.cmd spring-boot:run
