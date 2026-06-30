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
REM  lifeRecord backend Docker deploy
REM  Flow: mvn package -> upload JAR+Dockerfile -> compose build+up
REM  Usage: deploy-backend-docker.bat [env]
REM ============================================

set "SERVER_IP=123.207.210.150"
set "SERVER_USER=root"
set "SSH_KEY=D:\1\life-record\lifeRecord-deploy\record.pem"

set "COMPOSE_DIR=/www/wwwroot/myproject"
set "REMOTE_BUILD_DIR=%COMPOSE_DIR%"
set "IMAGE_NAME=myproject-record-backend"
set "IMAGE_TAG=latest"
set "CONTAINER_NAME=record-backend"
set "COMPOSE_SERVICE=record-backend"
set "JAR_NAME=record-0.0.1-SNAPSHOT.jar"

set "SSH_OPTS=-i "%SSH_KEY%" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR"

set "ENV=%~1"
if "%ENV%"=="" set "ENV=prod"

set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%..\record" >nul
if errorlevel 1 (
    echo [ERROR] backend dir not found: %SCRIPT_DIR%..\record
    exit /b 1
)
set "PROJECT_DIR=%CD%"
popd >nul
set "TARGET_DIR=%PROJECT_DIR%\target"
set "DOCKERFILE=%PROJECT_DIR%\Dockerfile"
set "DOCKERIGNORE=%PROJECT_DIR%\.dockerignore"

echo ================================
echo     lifeRecord backend Docker deploy
echo ================================
echo env       : %ENV%
echo server    : %SERVER_USER%@%SERVER_IP%
echo image     : %IMAGE_NAME%:%IMAGE_TAG%
echo container : %CONTAINER_NAME%
echo compose   : %COMPOSE_DIR%
echo service   : %COMPOSE_SERVICE%
echo build dir : %REMOTE_BUILD_DIR%
echo.

where ssh >nul 2>&1 || (echo [ERROR] ssh.exe not found & exit /b 1)
where scp >nul 2>&1 || (echo [ERROR] scp.exe not found & exit /b 1)
where mvn >nul 2>&1 || (echo [ERROR] mvn not found & exit /b 1)
if not exist "%SSH_KEY%" (
    echo [ERROR] SSH key not found: %SSH_KEY%
    echo Please copy record.pem to that path or edit SSH_KEY in this script.
    exit /b 1
)
if not exist "%DOCKERFILE%" (
    echo [ERROR] Dockerfile not found: %DOCKERFILE%
    exit /b 1
)

echo [1/5] Maven package...
pushd "%PROJECT_DIR%" >nul
call mvn clean package -DskipTests -Dmaven.test.skip=true
set "MVN_RET=%ERRORLEVEL%"
popd >nul
if not "%MVN_RET%"=="0" (
    echo [FAIL] mvn package failed
    exit /b 1
)
if not exist "%TARGET_DIR%\%JAR_NAME%" (
    echo [ERROR] JAR not found: %TARGET_DIR%\%JAR_NAME%
    dir /b "%TARGET_DIR%\*.jar" 2>nul
    exit /b 1
)
echo [OK] package success
echo.

echo [2/5] prepare remote build dir...
REM jar 远端目录用 recordJar，避免和本地 mvn 的 target 概念混淆
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "mkdir -p %REMOTE_BUILD_DIR%/recordJar && rm -f %REMOTE_BUILD_DIR%/recordJar/*.jar"
if errorlevel 1 (
    echo [FAIL] remote mkdir failed
    exit /b 1
)
echo [OK]
echo.

echo [3/5] upload JAR + Dockerfile...
scp %SSH_OPTS% "%TARGET_DIR%\%JAR_NAME%" "%SERVER_USER%@%SERVER_IP%:%REMOTE_BUILD_DIR%/recordJar/"
if errorlevel 1 ( echo [FAIL] JAR upload failed & exit /b 1 )
scp %SSH_OPTS% "%DOCKERFILE%" "%SERVER_USER%@%SERVER_IP%:%REMOTE_BUILD_DIR%/Dockerfile"
if errorlevel 1 ( echo [FAIL] Dockerfile upload failed & exit /b 1 )
if exist "%DOCKERIGNORE%" (
    scp %SSH_OPTS% "%DOCKERIGNORE%" "%SERVER_USER%@%SERVER_IP%:%REMOTE_BUILD_DIR%/.dockerignore"
)
echo [OK] upload done
echo.

echo [4/5] docker compose up --build...
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "cd %COMPOSE_DIR% && (docker compose up -d --no-deps --build --force-recreate %COMPOSE_SERVICE% || docker-compose up -d --no-deps --build --force-recreate %COMPOSE_SERVICE%)"
if errorlevel 1 (
    echo [FAIL] compose up failed, check %COMPOSE_DIR%
    exit /b 1
)
echo [OK] image built + container recreated
echo.

echo [5/5] prune dangling images...
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "docker image prune -f >/dev/null 2>&1; echo done"
echo.

echo Wait 10s for container start...
ping -n 11 127.0.0.1 >nul
echo [INFO] container status:
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "docker ps --filter name=%CONTAINER_NAME% --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
echo.
echo [INFO] last 20 log lines:
ssh %SSH_OPTS% "%SERVER_USER%@%SERVER_IP%" "docker logs --tail 20 %CONTAINER_NAME% 2>&1"
echo.

echo ================================
echo     backend deploy done
echo ================================
echo tail log: ssh -i "%SSH_KEY%" %SERVER_USER%@%SERVER_IP% "docker logs -f %CONTAINER_NAME%"
goto :eof
