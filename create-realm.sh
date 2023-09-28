#/usr/bin/env bash
set -e
#set -x

# Document:
#   https://access.redhat.com/documentation/ja-jp/red_hat_single_sign-on/7.6/html-single/server_administration_guide/index#admin_cli
#   `kcadm.sh --help`

KEYCLOAK_HOME=~/opt/java/keycloak-22.0.3
KEYCLOAK_SERVER=http://localhost:8180

REALM_NAME=test-realm
REALM_ADMIN=test-admin
REALM_ADMIN_PASSWORD=password

# Login
${KEYCLOAK_HOME}/bin/kcadm.sh config credentials --server ${KEYCLOAK_SERVER} --realm ${REALM_NAME} --user ${REALM_ADMIN} --password "${REALM_ADMIN_PASSWORD}"

function create_group() {
  local groupname="$1"
  local groupid
  groupid=$(${KEYCLOAK_HOME}/bin/kcadm.sh create groups -r ${REALM_NAME} -s name="${groupname}" --id)
  echo ${groupid}
}

function create_user() {
  local username="$1"
  local password="$2"
  local groupid="$3"
  local userid
  userid=$(${KEYCLOAK_HOME}/bin/kcadm.sh create users -r ${REALM_NAME} -s username="${username}" -s enabled=true --id)
  ${KEYCLOAK_HOME}/bin/kcadm.sh set-password -r ${REALM_NAME} --username "${username}" --new-password "${password}" --temporary
  ${KEYCLOAK_HOME}/bin/kcadm.sh update users/${userid}/groups/${groupid} -r ${REALM_NAME} -s userId=${userid} -s groupId=${groupid} --no-merge
  echo ${userid}
}

function create_realmrole() {
  local rolename="$1"
  local roleid
  roleid=$(${KEYCLOAK_HOME}/bin/kcadm.sh create roles -r ${REALM_NAME} -s name="${rolename}" --id)
  echo ${roleid}
}

# Create a group
GROUP_01=$(create_group "Team 01")

# Create users into the group
create_user testuser01 password ${GROUP_01}
create_user testuser02 password ${GROUP_01}
create_user testuser03 password ${GROUP_01}

# Create another group
GROUP_02=$(create_group "Team 02")

# Create users into the group
create_user testuser04 password ${GROUP_02}
create_user testuser05 password ${GROUP_02}
create_user testuser06 password ${GROUP_02}

# Role settings. See `kcadm.sh add-roles --help`
create_realmrole "normal"
create_realmrole "admin"
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r ${REALM_NAME} --gname "Team 01" --rolename "normal"
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r ${REALM_NAME} --gname "Team 01" --rolename "admin"
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r ${REALM_NAME} --gname "Team 02" --rolename "normal"
${KEYCLOAK_HOME}/bin/kcadm.sh add-roles -r ${REALM_NAME} --uusername testuser01 --cclientid realm-management --rolename view-users

