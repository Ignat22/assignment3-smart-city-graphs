#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac --release 17 -d out $(find src -name "*.java")

case "${1:-all}" in
  demo) java -cp out app.Main ;;
  test) java -cp out tests.AllTests ;;
  bench) java -cp out app.Benchmark ;;
  all)
    java -cp out tests.AllTests
    java -cp out app.Main
    java -cp out app.Benchmark
    ;;
  *) echo "Usage: ./run.sh [demo|test|bench|all]"; exit 1 ;;
esac
