@echo off
REM Reusable gradle wrapper for WSL->Windows builds. Usage: gw.bat <gradle args...>
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
cd /d D:\Dev\helloworld-unified
call gradlew.bat %*
exit /b %errorlevel%
