set /p TOKEN=<token.txt

curl -s -X GET "http://localhost:8080/api/user/profile" ^
     -H "Authorization: Bearer %TOKEN%" ^
     -H "Content-Type: application/json"
