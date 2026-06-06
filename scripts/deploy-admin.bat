@echo off
setlocal EnableDelayedExpansion

call :main %*
set "EXIT_CODE=%ERRORLEVEL%"
echo.
if not "%EXIT_CODE%"=="0" (
    echo ================================
    echo     FAILED [code: %EXIT_CODE%]
    echo ================================
)
if "%NO_PAUSE%"=="1" (
    endlocal ^& exit /b %EXIT_CODE%
)
echo Press any key to close...
pause >nul
endlocal ^& exit /b %EXIT_CODE%

:main
REM ============================================
REM  lifeRecord PC admin frontend deploy
REM  Usage: deploy-admin.bat [--skip-install]
REM ============================================

set "SERVER_IP=123.207.210.150"
set "SERVER_USER=root"
set "SSH_KEY=C:\Users\Administrator\Desktop\lifeRecord-deploy\record.pem"

set "REMOTE_DIR=/www/wwwroot/myproject/recordFront-admin"
set "REMOTE_TMP=/tmp/lifeRecord-admin-dist.tar.gz"

set "SSH_OPTS=-i "%SSH_KEY%" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR"

set "SKIP_INSTALL=0"
if /i "%~1"=="--skip-install" set "SKIP_INSTALL=1"

set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%..\admin" >nul
if errorlevel 1 (
    echo [ERROR] admin dir not found: %SCRIPT_DIR%..\admin
    exit /b 1
)
set "PROJECT_DIR=%CD%"
popd >nul
set "DIST_DIR=%PROJECT_DIR%\dist"
set "TAR_FILE=%PROJECT_DIR%\dist.tar.gz"

echo ================================
echo     lifeRecord PC admin deploy
echo ================================
echo server     : %SERVER_USER%@%SERVER_IP%
echo remote dir : %REMOTE_DIR%
echo source dir : %PROJECT_DIR%
echo skip install: %SKIP_INSTALL%
echo.

where ssh >nul 2>&1 || (echo [ERROR] ssh.exe not found & exit /b 1)
where scp >nul 2>&1 || (echo [ERROR] scp.exe not found & exit /b 1)
where tar >nul 2>&1 || (echo [ERROR] tar.exe not found & exit /b 1)
where pnpm >nul 2>&1 || (echo [ERROR] pnpm not found & exit /b 1)
if not exist "%SSH_KEY%" (
    echo [ERROR] SSH key not found: %SSH_KEY%
    exit /b 1
)

if "%SKIP_INSTALL%"=="0" (
    echo [1/5] pnpm install --frozen-lockfile ...
    pushd "%PROJECT_DIR%" >nul
    call pnpm install --frozen-lockfile
    set "PNPM_RET=!ERRORLEVEL!"
    popd >nul
    if not "!PNPM_RET!"=="0" (
        echo [FAIL] pnpm install failed
        exit /b 1
    )
    echo [OK] install done
) else (
    echo [1/5] skip install
)
echo.

echo [2/5] pnpm build ...
pushd "%PROJECT_DIR%" >nul
call pnpm build
set "BUILD_RET=%ERRORLEVEL%"
popd >nul
if not "%BUILD_RET%"=="0" (
    echo [FAIL] build failed
    exit /b 1
)
if not exist "%DIST_DIR%\index.html" (
    echo [ERROR] dist/index.html missing
    exit /b 1
)
echo [OK] build done
echo.

echo [3/5] tar dist ...
if exist "%TAR_FILE%" del /q "%TAR_FILE%"
pushd "%DIST_DIR%" >nul
tar -czf "%TAR_FILE%" .
set "TAR_RET=%ERRORLEVEL%"
popd >nul
if not "%TAR_RET%"=="0" (
    echo [FAIL] tar failed
    exit /b 1
)
echo [OK] tar done
echo.

echo [4/5] upload ...
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "mkdir -p %REMOTE_DIR%"
if errorlevel 1 (
    echo [FAIL] remote mkdir failed
    exit /b 1
)
scp %SSH_OPTS% "%TAR_FILE%" "%SERVER_USER%@%SERVER_IP%:%REMOTE_TMP%"
if errorlevel 1 (
    echo [FAIL] upload failed
    exit /b 1
)
echo [OK] upload done
echo.

echo [5/5] extract on remote ...
REM 注意：cmd 不识别 \" 转义，内部双引号必须用 "" (doubled quotes)，否则 2>/dev/null 会被 cmd 当作本地重定向，报 "系统找不到指定的路径"
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "set -e; BAK=%REMOTE_DIR%_backup_$(date +%%Y%%m%%d_%%H%%M%%S); if [ -d %REMOTE_DIR% ] && [ -n ""$(ls -A %REMOTE_DIR% 2>/dev/null)"" ]; then mv %REMOTE_DIR% ""$BAK"" && mkdir -p %REMOTE_DIR% && echo backup to $BAK; fi; tar -xzf %REMOTE_TMP% -C %REMOTE_DIR%/ && rm -f %REMOTE_TMP% && echo 'deploy ok'"
if errorlevel 1 (
    echo [FAIL] remote extract failed
    exit /b 1
)
echo [OK] extract done
echo.

del /q "%TAR_FILE%" >nul 2>&1

echo ================================
echo     admin deploy done
echo ================================
goto :eof
