@echo off

set /p TOKEN=<token.txt

set find='\"token\": \".*\"'
set replace='\"token\": \"%TOKEN%\"'

:: Find and replace token in debug.json
powershell -Command "(Get-Content debug.json) -replace %find%, %replace% | Set-Content debug.json"

curl -s -X POST "http://localhost:8080/api/debug/validate-token" ^
     -H "Content-Type: application/json" ^
     -d @debug.json | jq
