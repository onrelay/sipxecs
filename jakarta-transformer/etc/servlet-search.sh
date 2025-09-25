#!/bin/bash
# Usage: ./find_servlet_classes.sh /path/to/unpacked-dir-or.jar

INPUT="$1"

if [[ -z "$INPUT" ]]; then
    echo "Usage: $0 /path/to/unpacked-dir-or.jar"
    exit 1
fi

# If input is a JAR, unpack it into a temp dir
if [[ -f "$INPUT" && "$INPUT" == *.jar ]]; then
    TMPDIR=$(mktemp -d)
    echo "Unzipping $INPUT into $TMPDIR ..."
    unzip -qq "$INPUT" -d "$TMPDIR"
    SCANDIR="$TMPDIR"
elif [[ -d "$INPUT" ]]; then
    SCANDIR="$INPUT"
else
    echo "Error: $INPUT is not a jar file or directory"
    exit 1
fi

echo "Scanning $SCANDIR for javax.servlet usage..."

find "$SCANDIR" -type f -name '*.class' -print0 | while IFS= read -r -d '' classfile; do
    if grep -aq "javax/servlet" "$classfile"; then
        echo "$classfile"
    fi
done

echo "Scan complete."

# Clean up temp dir if we unzipped
if [[ -n "$TMPDIR" ]]; then
    rm -rf "$TMPDIR"
fi