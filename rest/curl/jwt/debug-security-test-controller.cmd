set /p TOKEN=<token.txt

set url=http://localhost:8080/api/test/has-role-admin
::set url=http://localhost:8080/api/test/has-authority-role-admin
::set url=http://localhost:8080/api/test/jsr-admin
::set url=http://localhost:8080/api/test/direct-check

curl -v -X GET "%url%" ^
     -H "Authorization: Bearer %TOKEN%"