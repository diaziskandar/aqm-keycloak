#!/bin/bash
# Keycloak Admin API Helper

KEYCLOAK_URL="http://localhost:8080"
REALM="erp-realm"
ADMIN_USER="admin"
ADMIN_PASS="admin"

# Get admin token
get_token() {
  curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
    -d "client_id=admin-cli" \
    -d "username=$ADMIN_USER" \
    -d "password=$ADMIN_PASS" \
    -d "grant_type=password" | jq -r '.access_token'
}

TOKEN=$(get_token)
echo "Token: $TOKEN"

# List all users
list_users() {
  curl -s -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM/users" | jq '.'
}

# Create user
create_user() {
  local USERNAME=$1
  local EMAIL=$2
  local PASS=$3
  
  curl -s -X POST -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"$USERNAME\",
      \"email\": \"$EMAIL\",
      \"firstName\": \"Test\",
      \"lastName\": \"User\",
      \"enabled\": true,
      \"credentials\": [{
        \"type\": \"password\",
        \"value\": \"$PASS\",
        \"temporary\": false
      }]
    }" \
    "$KEYCLOAK_URL/admin/realms/$REALM/users"
}

# Reset password
reset_password() {
  local USER_ID=$1
  local NEW_PASS=$2
  
  curl -s -X PUT -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"type\": \"password\",
      \"value\": \"$NEW_PASS\",
      \"temporary\": false
    }" \
    "$KEYCLOAK_URL/admin/realms/$REALM/users/$USER_ID/reset-password"
}

# List clients
list_clients() {
  curl -s -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/$REALM/clients" | jq '.'
}

case "$1" in
  users) list_users ;;
  create-user) create_user "$2" "$3" "$4" ;;
  reset-pass) reset_password "$2" "$3" ;;
  clients) list_clients ;;
  *) echo "Usage: $0 {users|create-user|reset-pass|clients}" ;;
esac
