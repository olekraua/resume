#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/../../.." && pwd)"
OUTPUT_BASE="${1:-${REPO_ROOT}/../resume-microservices-repos}"

SERVICES=(
  "auth-service"
  "profile-service"
  "search-service"
  "staticdata-service"
  "messaging-service"
  "notification-service"
  "outbox-relay-service"
)

modules_for_service() {
  case "$1" in
    auth-service|profile-service|search-service)
      echo "shared staticdata profile notification auth media web search"
      ;;
    staticdata-service)
      echo "shared staticdata profile notification auth media web"
      ;;
    messaging-service)
      echo "shared staticdata profile notification auth media web messaging"
      ;;
    notification-service)
      echo "shared notification"
      ;;
    outbox-relay-service)
      echo "shared staticdata profile notification auth search"
      ;;
    *)
      echo "Unknown service: $1" >&2
      exit 1
      ;;
  esac
}

rewrite_root_pom_modules() {
  local service="$1"
  local destination_pom="$2"
  shift 2
  local modules=("$@")
  local modules_string
  modules_string="$(printf "%s " "${modules[@]}")"

  awk -v modules="${modules_string}" -v service="${service}" '
    BEGIN {
      n = split(modules, m, " ")
      in_modules = 0
    }
    /<modules>/ {
      print "\t<modules>"
      for (i = 1; i <= n; i++) {
        if (length(m[i]) > 0) {
          print "\t\t<module>" m[i] "</module>"
        }
      }
      print "\t\t<module>microservices/backend/services/" service "</module>"
      print "\t</modules>"
      in_modules = 1
      next
    }
    in_modules && /<\/modules>/ {
      in_modules = 0
      next
    }
    !in_modules {
      print
    }
  ' "${REPO_ROOT}/pom.xml" > "${destination_pom}"
}

copy_directory() {
  local source="$1"
  local destination="$2"
  mkdir -p "$(dirname -- "${destination}")"
  rsync -a \
    --exclude='.DS_Store' \
    --exclude='target' \
    --exclude='.git' \
    "${source}/" "${destination}/"
}

write_service_readme() {
  local service="$1"
  local repo_dir="$2"
  local modules_bullets="$3"

  cat > "${repo_dir}/README.md" <<README
# ${service}

Standalone repository for the ${service} microservice.

## Local build

\`\`\`bash
./mvnw -pl microservices/backend/services/${service} -am -Dmaven.test.skip=true package
\`\`\`

## Local run

\`\`\`bash
./mvnw -pl microservices/backend/services/${service} -am spring-boot:run
\`\`\`

## Included modules

${modules_bullets}
README
}

mkdir -p "${OUTPUT_BASE}"

for service in "${SERVICES[@]}"; do
  repo_name="resume-${service}"
  repo_dir="${OUTPUT_BASE}/${repo_name}"

  modules_raw="$(modules_for_service "${service}")"
  IFS=' ' read -r -a modules <<< "${modules_raw}"

  echo "Preparing ${repo_name} in ${repo_dir}"
  rm -rf "${repo_dir}"
  mkdir -p "${repo_dir}"

  copy_directory "${REPO_ROOT}/.mvn" "${repo_dir}/.mvn"
  copy_directory "${REPO_ROOT}/config/checkstyle" "${repo_dir}/config/checkstyle"
  cp "${REPO_ROOT}/mvnw" "${repo_dir}/mvnw"
  cp "${REPO_ROOT}/mvnw.cmd" "${repo_dir}/mvnw.cmd"
  cp "${REPO_ROOT}/.gitignore" "${repo_dir}/.gitignore"
  chmod +x "${repo_dir}/mvnw"

  for module in "${modules[@]}"; do
    copy_directory "${REPO_ROOT}/${module}" "${repo_dir}/${module}"
  done

  copy_directory \
    "${REPO_ROOT}/microservices/backend/services/${service}" \
    "${repo_dir}/microservices/backend/services/${service}"

  rewrite_root_pom_modules "${service}" "${repo_dir}/pom.xml" "${modules[@]}"

  modules_bullets=""
  for module in "${modules[@]}"; do
    modules_bullets+="- ${module}"$'\n'
  done
  modules_bullets+="- microservices/backend/services/${service}"$'\n'

  write_service_readme "${service}" "${repo_dir}" "${modules_bullets}"

  git -C "${repo_dir}" init -q
  git -C "${repo_dir}" branch -M main >/dev/null 2>&1 || true
done

echo "Done. Standalone repositories are available in: ${OUTPUT_BASE}"
