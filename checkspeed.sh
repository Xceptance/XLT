#!/bin/bash

# Check if a directory was provided
TARGET_DIR=${1:-"."}

if [ ! -d "$TARGET_DIR" ]; then
    echo "Error: $TARGET_DIR is not a directory."
    exit 1
fi

echo "--- Calculating Total Size ---"
# -s for summary, -h for human-readable
du -sh "$TARGET_DIR"

echo -e "\n--- Measuring Read Speed ---"
echo "Reading all files in $TARGET_DIR..."

# 1. 'tar' bundles the files to stdout
# 2. 'pv' measures the data flowing through the pipe
# 3. '> /dev/null' tosses the data so we don't fill your screen
tar -cf - "$TARGET_DIR" 2>/dev/null | pv -ra > /dev/null

echo -e "\nDone."
