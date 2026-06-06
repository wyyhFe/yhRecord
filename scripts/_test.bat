@echo off
echo Hello from _test.bat
echo Current dir: %CD%
echo Script dir : %~dp0
where ssh
where scp
where mvn
where pnpm
where tar
where docker
echo.
echo --- press any key to close ---
pause >nul
