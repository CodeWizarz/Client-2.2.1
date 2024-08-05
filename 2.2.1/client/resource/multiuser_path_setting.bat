@echo off

set RES_DRIVE=R:
IF EXIST "%USERPROFILE%\Documents\" (
	set RES_PATH="%USERPROFILE%\Documents\Rapid Client"
) ELSE (
	set RES_PATH="%USERPROFILE%\My Documents\Rapid Client"
)

IF NOT EXIST %RES_PATH% (
	mkdir %RES_PATH%
)

IF NOT EXIST %RES_DRIVE% (
	subst %RES_DRIVE% %RES_PATH%
)