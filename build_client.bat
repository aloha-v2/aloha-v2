@echo off
echo.
echo  AlohaClient - Build Script
echo  ==========================
echo.

REM Clear potentially broken JAVA_HOME first
set JAVA_HOME=

REM Try to find a valid Java 16+ installation
REM Check common Android Studio / JetBrains JDK locations
set JAVA_CANDIDATES=^
D:\Android\jdk17\bin\java.exe ^
D:\Android\Android Studio\jbr\bin\java.exe ^
D:\Android\Android Studio\jre\bin\java.exe ^
C:\Program Files\Android\Android Studio\jbr\bin\java.exe ^
C:\Program Files\Android\Android Studio\jre\bin\java.exe ^
C:\Program Files\Eclipse Adoptium\jdk-17.0.6.10-hotspot\bin\java.exe ^
C:\Program Files\Eclipse Adoptium\jdk-17.0.5.8-hotspot\bin\java.exe ^
C:\Program Files\Java\jdk-17\bin\java.exe ^
C:\Program Files\Java\jdk-17.0.6\bin\java.exe ^
C:\Program Files\Microsoft\jdk-17.0.6.10-hotspot\bin\java.exe

for %%J in (%JAVA_CANDIDATES%) do (
    if exist "%%J" (
        echo  Found Java at: %%J
        for %%D in ("%%J\..") do set JAVA_HOME=%%~fD\..
        set JAVA_EXE=%%J
        goto :found
    )
)

REM If nothing found, use PATH java
where java >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo  Using java from PATH
    goto :build
)

echo  ERROR: No Java found!
echo  Please install Java 17 from: https://adoptium.net/
echo  (Choose: Temurin 17 - Windows x64 .msi installer)
pause
exit /b 1

:found
REM Set JAVA_HOME to the parent of bin
for %%J in ("%JAVA_EXE%") do set JAVA_HOME=%%~dpJ..
set JAVA_HOME=%JAVA_HOME:"=%
echo  JAVA_HOME set to: %JAVA_HOME%

:build
echo.
echo  Starting Gradle build...
echo  (First run downloads dependencies ~200MB, please wait)
echo.
call gradlew.bat build

if %ERRORLEVEL% == 0 (
    echo.
    echo  ========================
    echo  BUILD SUCCESSFUL!
    echo  JAR is in: build\libs\
    echo  ========================
) else (
    echo.
    echo  BUILD FAILED. See errors above.
)

echo.
pause
