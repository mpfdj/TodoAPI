:: Test login
curl -v -X POST "http://localhost:8080/login" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "username=admin@example.com&password=admin123"
