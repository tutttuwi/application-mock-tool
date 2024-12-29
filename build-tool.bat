@echo off

set "CURRENT_DIR=%~dp0"

call env.bat

rem java -cp %CURRENT_DIR%build FileMonitor 
javac %CURRENT_DIR%build-tool\\FileMonitor.java

exit 0
