#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${REPO_DIR}"

echo "=== FastRegex Benchmark Runner ==="

# Ensure lib directory or symlink is available
if [ ! -d "lib" ]; then
    echo "Creating symlink to lib..."
    ln -sfn /home/rschwietzke/projects/GIT/xlt-research-reporting/lib lib
fi

MODE="${1:-all}" # "isolated", "e2e", or "all"

if [ "$MODE" = "isolated" ] || [ "$MODE" = "all" ]; then
    echo "--- [1/2] Running Isolated MergeRule & URL Regex Benchmarks ---"
    rm -rf /tmp/bench-classes
    mkdir -p /tmp/bench-classes
    javac -cp "target/classes:lib/*" -d /tmp/bench-classes "${SCRIPT_DIR}/FastRegexEvalBenchmark.java"
    java -Xmx8g -cp "/tmp/bench-classes:target/classes:lib/*" -Dcom.xceptance.xlt.home="${REPO_DIR}" com.xceptance.xlt.report.mergerules.FastRegexEvalBenchmark
    rm -rf /tmp/bench-classes
fi

if [ "$MODE" = "e2e" ] || [ "$MODE" = "all" ]; then
    echo "--- [2/2] Running End-to-End Report Generation Benchmarks ---"
    for ds in ariat aritzia; do
        if [ "$ds" = "ariat" ]; then
            DIR="/home/rschwietzke/projects/loadtest/test-results/xlt-result-ariat-lt-2025-315-20251119-165727"
        else
            DIR="/home/rschwietzke/projects/loadtest/test-results/xlt-result-aritzia-lt-2025-317-20260202-220012"
        fi
        echo "Dataset: ${ds}"
        for mode in true false; do
            OUT="/tmp/bench-out-${ds}-${mode}"
            CACHE="/tmp/bench-cache-${ds}-${mode}"
            LOG="/tmp/bench-log-${ds}-${mode}.txt"
            rm -rf "${OUT}" "${CACHE}" "${LOG}"
            ./bin/create_report.sh -noCharts \
                -Dcom.xceptance.xlt.reportgenerator.fastRegex.enabled=${mode} \
                -Dcom.xceptance.xlt.reportgenerator.telemetryStore.dir="${CACHE}" \
                -o "${OUT}" "${DIR}" > "${LOG}" 2>&1
            PHASE1=$(grep -F "records read -" "${LOG}" | tail -n 1 || true)
            echo "  fastRegex=${mode}: ${PHASE1}"
            rm -rf "${OUT}" "${CACHE}" "${LOG}"
        done
    done
fi

echo "=== Benchmarks Complete ==="
