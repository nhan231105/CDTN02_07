@REM Maven wrapper script for Windows
@echo off

setlocal enabledelayedexpansion

if not exist "%USERPROFILE%\.m2\repository\org\apache\maven\wrapper\maven-wrapper\3.2.0\maven-wrapper-3.2.0.jar" (
    echo Downloading Maven wrapper...
    mkdir "%USERPROFILE%\.m2\repository\org\apache\maven\wrapper\3.2.0\" 2>nul
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%USERPROFILE%\.m2\repository\org\apache\maven\wrapper\3.2.0\maven-wrapper-3.2.0.jar')"
)

if not exist "%TEMP%\apache-maven-3.9.5" (
    echo Downloading Apache Maven 3.9.5...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.5/apache-maven-3.9.5-bin.zip', '%TEMP%\maven.zip')"
    powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%TEMP%'"
    del "%TEMP%\maven.zip"
)

set MAVEN_HOME=%TEMP%\apache-maven-3.9.5
set PATH=%MAVEN_HOME%\bin;%PATH%

call mvn %*
