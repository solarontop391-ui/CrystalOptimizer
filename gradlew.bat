@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################
set WRAPPER_JAR="%~dp0\gradle\wrapper\gradle-wrapper.jar"
set MAIN_CLASS=org.gradle.wrapper.GradleWrapperMain
"%JAVA_HOME%\bin\java" -classpath %WRAPPER_JAR% %MAIN_CLASS% %*
