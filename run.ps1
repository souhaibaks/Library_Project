$ErrorActionPreference = "Stop"
$JAVAFX_LIB = $env:JAVAFX_LIB

# Find MySQL JDBC driver
$MYSQL_DRIVER = $null

# First, check environment variable
if ($env:MYSQL_DRIVER) {
  if (Test-Path $env:MYSQL_DRIVER) {
    $MYSQL_DRIVER = $env:MYSQL_DRIVER
  }
}

# Check in lib_db directory (project-specific location)
if (-not $MYSQL_DRIVER) {
  if (Test-Path "lib_db\mysql-connector-j*.jar") {
    $MYSQL_DRIVER = (Get-ChildItem "lib_db\mysql-connector-j*.jar" | Select-Object -First 1).FullName
  } elseif (Test-Path "lib_db\mysql-connector*.jar") {
    $MYSQL_DRIVER = (Get-ChildItem "lib_db\mysql-connector*.jar" | Select-Object -First 1).FullName
  }
}

# Check common locations (optional - users can set MYSQL_DRIVER env var instead)
if (-not $MYSQL_DRIVER) {
  $commonPaths = @(
    "$env:USERPROFILE\Downloads\mysql-connector-j*.jar",
    "$env:USERPROFILE\Desktop\mysql-connector-j*.jar"
  )
  foreach ($path in $commonPaths) {
    $found = Get-ChildItem -Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) {
      $MYSQL_DRIVER = $found.FullName
      break
    }
  }
}

# Build classpath
$CLASSPATH = "bin;src\main\resources"
if ($MYSQL_DRIVER) {
  $CLASSPATH = "$CLASSPATH;$MYSQL_DRIVER"
  Write-Host "Found MySQL JDBC driver: $MYSQL_DRIVER"
} else {
  Write-Host "Warning: MySQL JDBC driver not found."
  Write-Host "Set MYSQL_DRIVER environment variable or place the JAR in lib_db directory."
  Write-Host "Database features will not work without the driver."
}

# --- Run-only mode ---
if ($args -contains "-RunOnly") {
  if (-not $JAVAFX_LIB) {
    if (Test-Path "lib") { $JAVAFX_LIB = (Resolve-Path "lib").Path } else { Write-Host "Error: JavaFX SDK not found. Set JAVAFX_LIB or provide lib directory."; exit 1 }
  }
  if (-not (Test-Path $JAVAFX_LIB)) { Write-Host "Error: JavaFX SDK not found at $JAVAFX_LIB"; exit 1 }
  Write-Host "Running application (run-only mode)..."
  & java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "$CLASSPATH" com.library.Main
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
  & java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "$CLASSPATH" com.library.Main
} else {
  Write-Host "Compilation failed."
  Remove-Item "sources.txt" -Force
  exit 1
}
