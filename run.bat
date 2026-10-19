@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"

set "JDK_BIN="

where javac >nul 2>nul
if not errorlevel 1 (
    javac --release 17 -version >nul 2>nul
    if not errorlevel 1 (
        set "JAVAC=javac"
        set "JAVA=java"
        goto build
    )
)

if defined JAVA_HOME call :try "%JAVA_HOME%"
if defined JDK_BIN goto found

for /d %%D in ("%ProgramFiles%\Java\*") do call :try "%%~D"
for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\*") do call :try "%%~D"
for /d %%D in ("%ProgramFiles%\Microsoft\*") do call :try "%%~D"
for /d %%D in ("%ProgramFiles%\Amazon Corretto\*") do call :try "%%~D"
for /d %%D in ("%ProgramFiles%\Zulu\*") do call :try "%%~D"
for /d %%D in ("%ProgramFiles%\BellSoft\*") do call :try "%%~D"
for /d %%D in ("%USERPROFILE%\.jdks\*") do call :try "%%~D"
if defined JDK_BIN goto found

echo.
echo JDK 17 or newer was not found.
echo Install it (for example from https://adoptium.net) or in IntelliJ:
echo File - Project Structure - SDK - Add SDK - Download JDK - version 17.
echo Then run this file again.
exit /b 1

:found
set "JAVAC=%JDK_BIN%\javac.exe"
set "JAVA=%JDK_BIN%\java.exe"
echo Using JDK: %JDK_BIN%

:build
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > sources.txt
"%JAVAC%" --release 17 -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    exit /b 1
)
del sources.txt

set "MODE=%~1"
if "%MODE%"=="" set "MODE=all"

if "%MODE%"=="demo" "%JAVA%" -cp out app.Main
if "%MODE%"=="test" "%JAVA%" -cp out tests.AllTests
if "%MODE%"=="bench" "%JAVA%" -cp out app.Benchmark
if "%MODE%"=="all" (
    "%JAVA%" -cp out tests.AllTests
    "%JAVA%" -cp out app.Main
    "%JAVA%" -cp out app.Benchmark
)
exit /b 0

:try
if exist "%~1\bin\javac.exe" (
    "%~1\bin\javac.exe" --release 17 -version >nul 2>nul
    if not errorlevel 1 set "JDK_BIN=%~1\bin"
)
exit /b 0
