; Confu Client Windows installer (per-user, no admin rights needed).
; The stub is x64. Pass -DARM64=1 for the Windows ARM64 package: the stub
; still runs under x64 emulation, the bundled JRE and LWJGL natives are aarch64.
; STAGE, OUTFILE, VERSION and ORIGINAL are passed with -D.
; Compressor is LZMA solid. Do not run UPX on this stub: SmartScreen
; treats UPX-packed unsigned installers as suspicious, and there is no
; certificate unless scripts/sign-windows.sh is given one.
Target amd64-unicode
SetCompressor /SOLID lzma
ManifestDPIAware true
ManifestSupportedOS all
!include "MUI2.nsh"
!include "x64.nsh"

!define APPNAME "Confu Client"
!ifndef VERSION
  !define VERSION "0.1.0"
!endif
!ifndef ORIGINAL
  !define ORIGINAL "Confu-Client-Setup-${VERSION}.exe"
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
!ifdef ARM64
  VIAddVersionKey "FileDescription" "${APPNAME} installer (Windows ARM64, native Java 17)"
  VIAddVersionKey "ProductVersion" "${VERSION} ARM64"
!else
  VIAddVersionKey "FileDescription" "${APPNAME} installer (Windows x64)"
  VIAddVersionKey "ProductVersion" "${VERSION}"
!endif
VIAddVersionKey "FileVersion" "${VERSION}"
VIAddVersionKey "CompanyName" "Confu Client"
VIAddVersionKey "LegalCopyright" "GPL-3.0, based on Glide and Soar"
VIAddVersionKey "InternalName" "ConfuClientSetup"
VIAddVersionKey "OriginalFilename" "${ORIGINAL}"

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
    MessageBox MB_ICONSTOP "${APPNAME} needs 64-bit Windows."
    Abort
  ${EndIf}
  !ifdef ARM64
    ${IfNot} ${IsNativeARM64}
      MessageBox MB_ICONSTOP "This is the Windows ARM64 build. On x64 Windows, use Confu-Client-Setup-${VERSION}-win-x64.exe."
      Abort
    ${EndIf}
  !else
    ${If} ${IsNativeARM64}
      MessageBox MB_OK|MB_ICONINFORMATION "This is the Windows x64 build. It runs here under emulation. For a native ARM64 Java 17 runtime, use Confu-Client-Setup-${VERSION}-win-arm64.exe."
    ${EndIf}
  !endif
FunctionEnd

Function StartLauncher
  Exec '"$INSTDIR\jre\bin\javaw.exe" -Dsun.java2d.dpiaware=true -jar "$INSTDIR\CONFUSION3H-launcher.jar"'
FunctionEnd

Section "Install"
  ; Rename the previous data folder before any file is written into .confu,
  ; so worlds and settings move with the name. If the rename fails (folder in
  ; use), do not create a second .confu: that would hide the old worlds.
  StrCpy $R9 "1"
  IfFileExists "$APPDATA\.confu\*" data_ready
  IfFileExists "$APPDATA\.CONFUSION3H\*" do_rename data_ready
  do_rename:
    ClearErrors
    Rename "$APPDATA\.CONFUSION3H" "$APPDATA\.confu"
    IfErrors 0 data_ready
      StrCpy $R9 "0"
  data_ready:

  SetOutPath "$INSTDIR"
  File "${STAGE}\CONFUSION3H-launcher.jar"
  File "${STAGE}\launch-compat.jar"
  File "${STAGE}\confusion3h.ico"
  File "${STAGE}\LICENSE.txt"
  !ifdef ARM64
    File "${STAGE}\java8-rt.jar"
    SetOutPath "$INSTDIR\natives\windows-arm64"
    File "${STAGE}\natives\windows-arm64\lwjgl64.dll"
    File "${STAGE}\natives\windows-arm64\OpenAL64.dll"
    SetOutPath "$INSTDIR"
  !endif
  SetOutPath "$INSTDIR\client"
  File "${STAGE}\client\CONFUSION3H-Release.jar"
  SetOutPath "$INSTDIR\jre"
  File /r "${STAGE}\jre\*"

  ; music folder in the game dir the launcher uses (%APPDATA%\.confu\minecraft)
  ; never overwrite the user's own files on reinstall
  StrCmp $R9 "0" music_skip
  SetOverwrite off
  SetOutPath "$APPDATA\.confu\minecraft\glide\music"
  File "${STAGE}\music\*"
  SetOverwrite on
  music_skip:

  SetOutPath "$INSTDIR"
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\jre\bin\javaw.exe" '-Dsun.java2d.dpiaware=true -jar "$INSTDIR\CONFUSION3H-launcher.jar"' "$INSTDIR\confusion3h.ico" 0
  CreateShortCut "$SMPROGRAMS\${APPNAME}\Music folder.lnk" "$APPDATA\.confu\minecraft\glide\music"
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
; %APPDATA%\.confu stay, like the vanilla launcher keeps .minecraft.
Section "Uninstall"
  Delete "$DESKTOP\${APPNAME}.lnk"
  RMDir /r "$SMPROGRAMS\${APPNAME}"
  RMDir /r "$INSTDIR\jre"
  RMDir /r "$INSTDIR\client"
  Delete "$INSTDIR\CONFUSION3H-launcher.jar"
  Delete "$INSTDIR\launch-compat.jar"
  Delete "$INSTDIR\java8-rt.jar"
  RMDir /r "$INSTDIR\natives"
  Delete "$INSTDIR\confusion3h.ico"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  DeleteRegKey HKCU "${UNINSTKEY}"
  DeleteRegKey HKCU "Software\${APPNAME}"
SectionEnd
