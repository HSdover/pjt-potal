@echo off
setlocal

cd /d "%~dp0..\.."
node tools\generator\generate-feature-from-config.mjs %*
set EXIT_CODE=%ERRORLEVEL%

echo.
exit /b %EXIT_CODE%
