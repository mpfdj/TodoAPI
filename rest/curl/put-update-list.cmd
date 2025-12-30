::Update a list
curl -v -X PUT "http://localhost:8080/users/1/lists/1" ^
     -H "Content-Type: application/json" ^
     -d @UpdateList.json | jq
