# With USER token (should succeed)
curl -X GET 'http://localhost:8080/api/user/profile' \
-H "Authorization: Bearer $USER_TOKEN" \
-H 'Content-Type: application/json'

# With ADMIN token (should succeed)
curl -X GET 'http://localhost:8080/api/user/profile' \
-H "Authorization: Bearer $ADMIN_TOKEN" \
-H 'Content-Type: application/json'

# Without token (should fail with 401)
curl -X GET 'http://localhost:8080/api/user/profile' \
-H 'Content-Type: application/json' \
-w "\nHTTP Status: %{http_code}\n"