$ErrorActionPreference = "Stop"
$JAVAFX_LIB = $env:JAVAFX_LIB

# Find MySQL JDBC driver
$MYSQL_DRIVER = $null
$MYSQL_DRIVER_PATH = "C:\Users\AMBEN\Desktop\mysql-connector-j-8.0.33"

# Check if it's a JAR file directly
if (Test-Path "$MYSQL_DRIVER_PATH.jar") {
  $MYSQL_DRIVER = "$MYSQL_DRIVER_PATH.jar"
} elseif (Test-Path $MYSQL_DRIVER_PATH) {
  # Check if it's a directory containing JAR files
  if ((Get-Item $MYSQL_DRIVER_PATH).PSIsContainer) {
    $jarFile = Get-ChildItem -Path $MYSQL_DRIVER_PATH -Filter "*.jar" -Recurse | Select-Object -First 1
    if ($jarFile) {
      $MYSQL_DRIVER = $jarFile.FullName
    }
  } else {
    # It's a file, use it directly
    $MYSQL_DRIVER = $MYSQL_DRIVER_PATH
  }
}

# Also check in lib_db directory
if (-not $MYSQL_DRIVER) {
  if (Test-Path "lib_db\mysql-connector-j*.jar") {
    $MYSQL_DRIVER = (Get-ChildItem "lib_db\mysql-connector-j*.jar" | Select-Object -First 1).FullName
  } elseif (Test-Path "lib_db\mysql-connector*.jar") {
    $MYSQL_DRIVER = (Get-ChildItem "lib_db\mysql-connector*.jar" | Select-Object -First 1).FullName
  }
}

# Build classpath
$CLASSPATH = "bin;src\main\resources"
if ($MYSQL_DRIVER) {
  $CLASSPATH = "$CLASSPATH;$MYSQL_DRIVER"
  Write-Host "Found MySQL JDBC driver: $MYSQL_DRIVER"
} else {
  Write-Host "Warning: MySQL JDBC driver not found at $MYSQL_DRIVER_PATH"
  Write-Host "Database features will not work. Please verify the driver location."
}

# --- Run-only mode ---
if ($args -contains "-RunOnly") {
  if (-not $JAVAFX_LIB) {
    if (Test-Path "lib") { $JAVAFX_LIB = (Resolve-Path "lib").Path } else { Write-Host "Error: JavaFX SDK not found. Set JAVAFX_LIB or provide lib directory."; exit 1 }
  }
  if (-not (Test-Path $JAVAFX_LIB)) { Write-Host "Error: JavaFX SDK not found at $JAVAFX_LIB"; exit 1 }
  Write-Host "Running application (run-only mode)..."
  & java '-Dprism.order=sw' '-Dprism.verbose=true' --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "$CLASSPATH" com.library.Main
  exit $LASTEXITCODE
}

if (-not $JAVAFX_LIB) {
  if (Test-Path "lib") { $JAVAFX_LIB = (Resolve-Path "lib").Path } else { Write-Host "Error: JavaFX SDK not found. Set JAVAFX_LIB or provide lib directory."; exit 1 }
}
if (-not (Test-Path $JAVAFX_LIB)) { Write-Host "Error: JavaFX SDK not found at $JAVAFX_LIB"; exit 1 }
Write-Host "Using JavaFX SDK at: $JAVAFX_LIB"
New-Item -ItemType Directory -Force -Path "bin" | Out-Null
Get-ChildItem "bin" -Recurse -ErrorAction SilentlyContinue | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
Write-Host "Finding source files..."
Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName | Set-Content "sources.txt"
Write-Host "Compiling..."
& javac --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -d "bin" -sourcepath "src\main\java" "@sources.txt"
if ($LASTEXITCODE -eq 0) {
  Write-Host "Compilation successful."
  Remove-Item "sources.txt" -Force
  Write-Host "Running application..."
  & java '-Dprism.order=sw' '-Dprism.verbose=true' --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "$CLASSPATH" com.library.Main
} else {
  Write-Host "Compilation failed."
  Remove-Item "sources.txt" -Force
  exit 1
}
