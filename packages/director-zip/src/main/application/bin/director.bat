@echo off

REM Developers can start Trust Director directly from build directory by setting
REM DIRECTOR_FS_JAVA=C:\path\to\dcg_security-director\packages\director\target\feature\java

REM SETLOCAL ENABLEEXTENSIONS
IF NOT DEFINED DIRECTOR_PASSWORD ECHO MUST SET DIRECTOR_PASSWORD && EXIT /B 1
IF NOT DEFINED DIRECTOR_HOME SET DIRECTOR_HOME=C:\director
IF NOT DEFINED DIRECTOR_FS_JAVA SET DIRECTOR_FS_JAVA=%DIRECTOR_HOME%\java
IF NOT DEFINED JVM_ARGS SET JVM_ARGS=-Xms128m -Xmx2048m -XX:MaxPermSize=128m -Dlogback.configurationFile=%DIRECTOR_HOME%\configuration\logback.xml
REM ENDLOCAL

ECHO DIRECTOR_HOME=%DIRECTOR_HOME%
ECHO DIRECTOR_FS_JAVA=%DIRECTOR_FS_JAVA%
ECHO JVM_ARGS=%JVM_ARGS%

java %JVM_ARGS% -cp %DIRECTOR_FS_JAVA%/* com.intel.mtwilson.launcher.console.Main %*
