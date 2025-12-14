:: Delete a user by Email
curl -v -X DELETE "http://localhost:8080/users?email=test@gmail.com" | jq
