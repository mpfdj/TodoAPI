:: Get token with USER role
curl -s -X GET "http://localhost:8080/api/public/tokens/user" | jq -r ".token" > token.txt
