:: Get token with ADMIN role
curl -s -X GET "http://localhost:8080/api/public/tokens/admin" | jq -r ".token" > token.txt
