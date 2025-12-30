::Update a user
curl -v -X PUT "http://localhost:8080/users/1" ^
     -H "Content-Type: application/json" ^
     -d @UpdateUser.json | jq
