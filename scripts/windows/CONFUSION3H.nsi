; CONFUSION3H Windows x64 installer (per-user, no admin rights needed)
; Built by scripts/build-windows-installer.sh; STAGE and OUTFILE are passed with -D.
Target amd64-unicode   ; native x64 installer (x64 only, no ARM)
SetCompressor /SOLID lzma
!include "MUI2.nsh"
!include "x64.nsh"

!define APPNAME "CONFUSION3H"
!ifndef VERSION
  !define VERSION "0.1.0"
!endif
!define UNINSTKEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"

Name "${APPNAME}"
OutFile "${OUTFILE}"
InstallDir "$LOCALAPPDATA\Programs\${APPNAME}"
InstallDirRegKey HKCU "Software\${APPNAME}" "InstallDir"
RequestExecutionLevel user
BrandingText "${APPNAME} ${VERSION}"
VIProductVersion "${VERSION}.0"
VIAddVersionKey "ProductName" "${APPNAME}"
VIAddVersionKey "FileDescription" "${APPNAME} installer"
VIAddVersionKey "FileVersion" "${VERSION}"
VIAddVersionKey "LegalCopyright" "GPL-3.0, based on Glide and Soar"

!define MUI_ICON "${STAGE}\confusion3h.ico"
!define MUI_UNICON "${STAGE}\confusion3h.ico"
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION StartLauncher
!define MUI_FINISHPAGE_RUN_TEXT "Start ${APPNAME}"
!insertmacro MUI_PAGE_LICENSE "${STAGE}\LICENSE.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "English"

Function .onInit
  ${IfNot} ${RunningX64}
    MessageBox MB_ICONSTOP "${APPNAME} needs 64-bit Windows (x64)."
    Abort
  ${EndIf}
FunctionEnd

Function StartLauncher
  Exec '"$INSTDIR\jre\bin\javaw.exe" -Dsun.java2d.dpiaware=true -jar "$INSTDIR\CONFUSION3H-launcher.jar"'
FunctionEnd

Section "Install"
  SetOutPath "$INSTDIR"
  File "${STAGE}\CONFUSION3H-launcher.jar"
  File "${STAGE}\confusion3h.ico"
  File "${STAGE}\LICENSE.txt"
  SetOutPath "$INSTDIR\client"
  File "${STAGE}\client\CONFUSION3H-Release.jar"
  SetOutPath "$INSTDIR\jre"
  File /r "${STAGE}\jre\*"

  ; music folder in the game dir the launcher uses (%APPDATA%\.CONFUSION3H\minecraft)
  ; never overwrite the user's own files on reinstall
  SetOverwrite off
  SetOutPath "$APPDATA\.CONFUSION3H\minecraft\glide\music"
  File "${STAGE}\music\*"
  SetOverwrite on

  SetOutPath "$INSTDIR"
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\jre\bin\javaw.exe" '-Dsun.java2d.dpiaware=true -jar "$INSTDIR\CONFUSION3H-launcher.jar"' "$INSTDIR\confusion3h.ico" 0
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Music folder.lnk" "$APPDATA\.CONFUSION3H\minecraft\glide\music"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Uninstall ${APPNAME}.lnk" "$INSTDIR\Uninstall.exe"
  CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\jre\bin\javaw.exe" '-Dsun.java2d.dpiaware=true -jar "$INSTDIR\CONFUSION3H-launcher.jar"' "$INSTDIR\confusion3h.ico" 0

  WriteUninstaller "$INSTDIR\Uninstall.exe"
  WriteRegStr HKCU "Software\${APPNAME}" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "${UNINSTKEY}" "DisplayName" "${APPNAME}"
  WriteRegStr HKCU "${UNINSTKEY}" "DisplayVersion" "${VERSION}"
  WriteRegStr HKCU "${UNINSTKEY}" "Publisher" "${APPNAME}"
  WriteRegStr HKCU "${UNINSTKEY}" "DisplayIcon" "$INSTDIR\confusion3h.ico"
  WriteRegStr HKCU "${UNINSTKEY}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKCU "${UNINSTKEY}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKCU "${UNINSTKEY}" "QuietUninstallString" '"$INSTDIR\Uninstall.exe" /S'
  WriteRegDWORD HKCU "${UNINSTKEY}" "NoModify" 1
  WriteRegDWORD HKCU "${UNINSTKEY}" "NoRepair" 1
SectionEnd

; Removes the program only. Worlds, settings, music and capes in
; %APPDATA%\.CONFUSION3H stay, like the vanilla launcher keeps .minecraft.
Section "Uninstall"
  Delete "$DESKTOP\${APPNAME}.lnk"
  RMDir /r "$SMPROGRAMS\${APPNAME}"
  RMDir /r "$INSTDIR\jre"
  RMDir /r "$INSTDIR\client"
  Delete "$INSTDIR\CONFUSION3H-launcher.jar"
  Delete "$INSTDIR\confusion3h.ico"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  DeleteRegKey HKCU "${UNINSTKEY}"
  DeleteRegKey HKCU "Software\${APPNAME}"
SectionEnd
