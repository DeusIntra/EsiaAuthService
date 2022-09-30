@rem
@rem Copyright 2004-2015 Crypto-Pro. All rights reserved.
@rem Этот файл содержит информацию, являющуюся
@rem собственностью компании Крипто-Про.
@rem
@rem Любая часть этого файла не может быть скопирована,
@rem исправлена, переведена на другие языки,
@rem локализована или модифицирована любым способом,
@rem откомпилирована, передана по сети с или на
@rem любую компьютерную систему без предварительного
@rem заключения соглашения с компанией Крипто-Про.
@rem
@rem ---------------------------------------------------
@rem
@rem Скрипт установки КриптоПро JCP v.2.0
@rem 
@rem Использование:
@rem   setup_console.bat <путь_к_JRE>
@rem
@rem Пример:
@rem   setup_console.bat "C:\Program Files\Java\jre7"
@rem

@set DELFILESLST=del.list

@if not "-%~1"=="-" @goto :checkjcp
@echo USAGE:
@echo   1. Interractive mode.
@echo     setup_console.bat path_to_JRE
@echo      Example:
@echo         setup_console.bat "C:\Program Files\Java\jre7"
@echo   1. Force (silent) mode.
@echo     "setup_console.bat path_to_JRE -force [-ru | -en] [-install | -uninstall] [-jre <value>] [-jcp | -jcryptop | -cpssl | -cades | -ocf | -j6cf | -jcsp] [-strict_mode] [-default_provider [0|1]] [-serial_jcp <value> -serial_cpssl <value> -serial_jcsp <value>] [-rmsetting]"
@echo      Examples:
@echo         1) install JCP (variant 2), cpSSL and CAdES into "C:\Program Files\Java\jre7" with serial number:
@echo           setup_console.bat "C:\Program Files\Java\jre7" -force -ru -install -jre "C:\Program Files\Java\jre7" -jcp -jcryptop -cpssl -cades -serial_jcp XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
@echo         2) uninstall JCP from default JRE (current JRE) "C:\Program Files\Java\jre7" and remove all saved settings: 
@echo           setup_console.bat "C:\Program Files\Java\jre7" -force -en -uninstall -jcp -rmsetting
@echo         3) install Java CSP into default JRE (current JRE) with serial number:
@echo           setup_console.bat "C:\Program Files\Java\jre7" -force -en -install -jcsp -serial_jcsp XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
@goto :EOF

:checkjcp
@echo Params: %*
@set DISTPATH=%~dp0
@if exist "%DISTPATH%\JCPinstGUI.jar" @goto :checkjcpinst
@echo Script must be in the installer folder
@goto :ERROR

:checkjcp
@echo Params: %*
@set DISTPATH=%~dp0
@if exist "%DISTPATH%\JCP.jar" @goto :checkjcpinst
@echo Script must be in the installer folder
@goto :ERROR

:checkjcpinst
@if exist "%DISTPATH%\JCPinst.jar" @goto :setjavacmd
@echo Script must be in the installer folder
@goto :ERROR

:setjavacmd
@set JAVACMD=java
@set JREDIR=
@if not "-%~1"=="-" @set JREDIR=%~1
@if not "-%JREDIR%"=="-" @goto :checkjre

:checkjre
@set JAVACMD="%JREDIR%\bin\java.exe"
@if exist %JAVACMD% @goto :printversion
@echo File not found: %JAVACMD%
@goto :ERROR

:printversion
@%JAVACMD% -version
@dir "%DISTPATH%"

:dellist
@if not exist %DELFILESLST% @goto :action
@for /F "tokens=*" %%i IN (%DELFILESLST%) DO @del /q /f "%%i"
@for /F "tokens=*" %%i IN (%DELFILESLST%) DO @if exist "%%i" @goto :stopjava
@del /q /f "%DELFILESLST%"

:action
@echo ---- Fulfilment started
@%JAVACMD% -Xbootclasspath/a:"%DISTPATH%\forms_rt.jar";"%DISTPATH%\JCPinstGUI.jar";"%DISTPATH%\JCPinst.jar";"%DISTPATH%\asn1rt.jar";"%DISTPATH%\ASN1P.jar";"%DISTPATH%\JCP.jar"; ru.CryptoPro.Installer.InstallerConsole %*
@if "%errorlevel%"=="0" @goto :installend
@if "%errorlevel%"=="2" @goto :restart
@echo Fulfilment failed
@goto :ERROR

@rem require deleting of locked files and continue
:restart
@goto dellist

:installend
@echo ---- Installer finished
@echo ---- Script SUCCEEDED
@goto :EOF

:stopjava
@echo ---- Warning
@echo JVM is running or jar-files are locked
@echo Stop all processes that use Java and retry
@goto :ERROR

:ERROR
@echo ---- Script ERROR 
@echo ---- error code: %errorlevel%
@goto :EOF

@goto :EOF
