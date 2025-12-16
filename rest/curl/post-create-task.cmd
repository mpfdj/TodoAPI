::Create a new task
curl -v -X POST "http://localhost:8080/users/1/lists/1/tasks" ^
     -H "Content-Type: application/json" ^
     -d @CreateTask.json | jq
