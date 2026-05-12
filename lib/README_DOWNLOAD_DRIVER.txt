MySQL JDBC Driver — One-Time Setup
====================================

The MySQL Connector/J JAR must be placed in this folder before compiling.

STEP 1 — Download the JAR (choose one method):

  Option A — Direct browser download:
    https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

  Option B — Command line (Windows PowerShell):
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -OutFile "lib\mysql-connector-j-8.0.33.jar"

  Option C — Command line (Linux / macOS):
    curl -L "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -o lib/mysql-connector-j-8.0.33.jar

STEP 2 — Verify the file is in the correct location:
    prms/
    └── lib/
        └── mysql-connector-j-8.0.33.jar   ← must be here

STEP 3 — Then run compile.bat, followed by run.bat.

NOTE: Do NOT rename the JAR file. compile.bat and run.bat use "lib/*" which
picks up all JARs automatically — any correctly named .jar in lib/ will work.
