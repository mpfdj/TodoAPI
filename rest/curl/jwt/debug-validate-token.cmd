@echo off

set /p TOKEN=<token.txt

set find='\"token\": \".*\"'
set replace='\"token\": \"%TOKEN%\"'

powershell -Command "(Get-Content debug.json) -replace %find%, %replace% | Set-Content debug.json"

curl -s -X POST "http://localhost:8080/api/debug/validate-token" ^
     -H "Content-Type: application/json" ^
     -d @debug.json | jq
