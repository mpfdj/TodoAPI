@echo off

set /p TOKEN=<token.txt

set url=http://localhost:8080/api/token/info
::set url=http://localhost:8080/api/token/expiry-check

curl -X GET "%url%" ^
     -H "Authorization: Bearer %TOKEN%" ^
     -H "Content-Type: application/json" | jq
