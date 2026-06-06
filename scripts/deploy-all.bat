@echo off
setlocal EnableDelayedExpansion

REM lifeRecord all-in-one deploy: backend then admin

set "SCRIPT_DIR=%~dp0"
set "BACKEND_SCRIPT=%SCRIPT_DIR%deploy-backend-docker.bat"
set "ADMIN_SCRIPT=%SCRIPT_DIR%deploy-admin.bat"

if not exist "%BACKEND_SCRIPT%" (
    echo [ERROR] backend script not found: %BACKEND_SCRIPT%
    goto :fail
)
if not exist "%ADMIN_SCRIPT%" (
    echo [ERROR] admin script not found: %ADMIN_SCRIPT%
    goto :fail
)

set "BACKEND_ENV=%~1"
if "%BACKEND_ENV%"=="" set "BACKEND_ENV=prod"

set "NO_PAUSE=1"
set "START_TS=%TIME%"

echo ================================
echo     lifeRecord full deploy
echo ================================
echo backend env: %BACKEND_ENV%
echo admin args : %2 %3 %4 %5 %6 %7 %8 %9
echo start time : %DATE% %TIME%
echo.

echo ###### [A] backend (Docker) ######
call "%BACKEND_SCRIPT%" %BACKEND_ENV%
set "STEP_RET=%ERRORLEVEL%"
if not "%STEP_RET%"=="0" (
    echo.
    echo [FAIL] backend deploy failed [code: %STEP_RET%], abort
    goto :fail
)
echo.

echo ###### [B] PC admin frontend ######
call "%ADMIN_SCRIPT%" %2 %3 %4 %5 %6 %7 %8 %9
set "STEP_RET=%ERRORLEVEL%"
if not "%STEP_RET%"=="0" (
    echo.
    echo [FAIL] admin deploy failed [code: %STEP_RET%]
    goto :fail
)
echo.

echo ================================
echo     full deploy done
echo ================================
echo start: %START_TS%
echo end  : %TIME%
echo.
echo Press any key to close...
pause >nul
endlocal ^& exit /b 0

:fail
echo.
echo ================================
echo     deploy aborted, see logs above
echo ================================
echo Press any key to close...
pause >nul
endlocal ^& exit /b 1
