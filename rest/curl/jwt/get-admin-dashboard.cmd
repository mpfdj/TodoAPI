set /p TOKEN=<token.txt

curl -v -X GET "http://localhost:8080/api//admin/dashboard" ^
     -H "Authorization: Bearer %TOKEN%" ^
     -H "Content-Type: application/json"
