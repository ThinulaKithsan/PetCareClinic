#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mvn clean verify
printf 'Run: java -jar target/petcare-clinic-3.0.0.jar\n'
