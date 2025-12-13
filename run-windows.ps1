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

# Check common locations
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

# Find JavaFX SDK - Windows version
if (-not $JAVAFX_LIB) {
  # Try lib_win first (Windows-specific), then lib
  if (Test-Path "lib_win") { 
    $JAVAFX_LIB = (Resolve-Path "lib_win").Path 
    Write-Host "Using Windows JavaFX libraries from lib_win"
  } elseif (Test-Path "lib") { 
    $JAVAFX_LIB = (Resolve-Path "lib").Path 
    Write-Host "Using JavaFX libraries from lib"
  } else { 
    Write-Host "Error: JavaFX SDK not found. Set JAVAFX_LIB or provide lib_win/lib directory."; exit 1 
  }
}
if (-not (Test-Path $JAVAFX_LIB)) { Write-Host "Error: JavaFX SDK not found at $JAVAFX_LIB"; exit 1 }
Write-Host "Using JavaFX SDK at: $JAVAFX_LIB"

# Try to find JavaFX SDK bin directory for native libraries (Windows)
$JAVAFX_BIN = $null
if ($JAVAFX_LIB -like "*\lib_win" -or $JAVAFX_LIB -like "*/lib_win") {
  $possibleBin = $JAVAFX_LIB -replace "\\lib_win$", "\bin" -replace "/lib_win$", "/bin"
  if (Test-Path $possibleBin) {
    $JAVAFX_BIN = $possibleBin
  }
} elseif ($JAVAFX_LIB -like "*\lib" -or $JAVAFX_LIB -like "*/lib") {
  $possibleBin = $JAVAFX_LIB -replace "\\lib$", "\bin" -replace "/lib$", "/bin"
  if (Test-Path $possibleBin) {
    $JAVAFX_BIN = $possibleBin
  }
}

# Check for full JavaFX SDK installation
if (-not $JAVAFX_BIN) {
  $possibleSDKPaths = @(
    "$env:ProgramFiles\Java\javafx-sdk-17\bin",
    "$env:LOCALAPPDATA\Programs\Java\javafx-sdk-17\bin",
    "C:\Program Files\Java\javafx-sdk-17\bin"
  )
  foreach ($sdkPath in $possibleSDKPaths) {
    if (Test-Path $sdkPath) {
      $JAVAFX_BIN = $sdkPath
      Write-Host "Found JavaFX SDK bin directory: $JAVAFX_BIN"
      break
    }
  }
}

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
  
  # Build Java arguments for Windows
  $javaArgs = @(
    "--module-path", "$JAVAFX_LIB",
    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics"
  )
  
  # Add native library path if found
  if ($JAVAFX_BIN) {
    $javaArgs += "-Djava.library.path=$JAVAFX_BIN"
  }
  
  # Windows-specific graphics pipeline settings
  # Use software renderer which works without native DLLs
  $javaArgs += "-Dprism.order=sw"
  $javaArgs += "-Djava.awt.headless=false"
  $javaArgs += "-cp", "$CLASSPATH", "com.library.Main"
  
  Write-Host "Starting application with software renderer..."
  Write-Host ""
  Write-Host "Note: If you see graphics pipeline errors, you may need to download"
  Write-Host "the full JavaFX SDK (including bin directory with DLL files) from:"
  Write-Host "https://openjfx.io/"
  Write-Host ""
  
  & java $javaArgs
  $exitCode = $LASTEXITCODE
  
  if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "Application failed to start. Common solutions:"
    Write-Host "1. Download full JavaFX SDK from https://openjfx.io/ (includes native DLLs)"
    Write-Host "2. Extract the SDK and set JAVAFX_LIB to point to the lib directory"
    Write-Host "3. Ensure the SDK's bin directory (with DLL files) is accessible"
  }
  
  exit $exitCode
} else {
  Write-Host "Compilation failed."
  Remove-Item "sources.txt" -Force
  exit 1
}
