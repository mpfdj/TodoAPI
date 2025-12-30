::Update a task
curl -v -X PUT "http://localhost:8080/users/1/lists/1/tasks/1" ^
     -H "Content-Type: application/json" ^
     -d @UpdateTask.json | jq
