@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
cd /d C:\AndroidSDK\Sdk\cmdline-tools\latest\bin
echo y| sdkmanager.bat "platforms;android-33" "build-tools;33.0.1"
