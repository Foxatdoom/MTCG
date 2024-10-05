#!/bin/sh

# --------------------------------------------------
# Monster Trading Cards Game
# --------------------------------------------------
echo "CURL Testing for Monster Trading Cards Game"
echo "Syntax: MonsterTradingCards.sh [pause]"
echo "- pause: optional, if set, then script will pause after each block"
echo .


pauseFlag=0
for arg in "$@"; do
    if [ "$arg" == "pause" ]; then
        pauseFlag=1
        break
    fi
done

if [ $pauseFlag -eq 1 ]; then read -p "Press enter to continue..."; fi

# --------------------------------------------------
echo "1) Create Users (Registration)"
# Create User
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"daniel\"}"


echo "Should return HTTP 201"
echo .
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"altenhof\", \"Password\":\"markus\"}"
echo "Should return HTTP 201"
echo .
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"admin\",    \"Password\":\"istrator\"}"
echo "Should return HTTP 201"
echo .

if [ $pauseFlag -eq 1 ]; then read -p "Press enter to continue..."; fi

echo "should fail:"
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"daniel\"}"
echo "Should return HTTP 4xx - User already exists"
echo .
curl -i -X POST http://localhost:10001/users --header "Content-Type: application/json" -d "{\"Username\":\"kienboec\", \"Password\":\"different\"}"
echo "Should return HTTP 4xx - User already exists"
echo . 
echo .
