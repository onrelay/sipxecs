# From your lib directory
for jar in *.jar; do
  if jar tf "$jar" | grep -q "javax/servlet/"; then
    echo "$jar"
  fi
done
