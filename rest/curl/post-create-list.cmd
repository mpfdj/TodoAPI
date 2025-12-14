::Create a new list
curl -v -X POST "http://localhost:8080/users/1/lists" ^
     -H "Content-Type: application/json" ^
     -d @CreateList.json | jq
