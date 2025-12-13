$ErrorActionPreference = "Stop"
$JAVAFX_LIB = $env:JAVAFX_LIB
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
  & java '-Dprism.order=sw' '-Dprism.verbose=true' --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "bin;src\main\resources" com.library.Main
} else {
  Write-Host "Compilation failed."
  Remove-Item "sources.txt" -Force
  exit 1
}
