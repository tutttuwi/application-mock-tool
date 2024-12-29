@echo off

set "CURRENT_DIR=%~dp0"

call env.bat

java -cp %CURRENT_DIR%build-tool FileMonitor C:\\git\\node\\application-mock-tool\\src C:\\git\\node\\application-mock-tool\\dist .*.html

pause

exit 0
