::Create a new user
curl -v -X POST "http://localhost:8080/users" ^
     -H "Content-Type: application/json" ^
     -d @CreateUser.json | jq
