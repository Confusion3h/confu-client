import { statSync } from "fs";
import path from "path";

export type Release = {
  os: string;
  arch: string;
  file: string;
  detail: string;
  bytes: number;
};

const FILES: Omit<Release, "bytes">[] = [
  {
    os: "Windows",
    arch: "x64",
    file: "Confu-Client-Setup-0.1.0-win-x64.exe",
    detail: "Installer · Temurin 8",
  },
  {
    os: "Windows",
    arch: "ARM64",
    file: "Confu-Client-Setup-0.1.0-win-arm64.exe",
    detail: "Installer · Java 17, native",
  },
  {
    os: "macOS",
    arch: "Apple Silicon",
    file: "Confu-Client-0.1.0-mac-aarch64.zip",
    detail: "App zip · Zulu 8",
  },
  {
    os: "macOS",
    arch: "Intel",
    file: "Confu-Client-0.1.0-mac-x64.zip",
    detail: "App zip · Temurin 8",
  },
  {
    os: "Linux",
    arch: "x64",
    file: "Confu-Client-0.1.0-linux-x64.tar.gz",
    detail: "Archive · Temurin 8",
  },
  {
    os: "Linux",
    arch: "ARM64",
    file: "Confu-Client-0.1.0-linux-aarch64.tar.gz",
    detail: "Archive · Temurin 8",
  },
];

export const RELEASE_FILES = new Set(FILES.map((item) => item.file));

export function releasesDir() {
  return path.resolve(process.cwd(), "..", "releases");
}

export function loadReleases(): Release[] {
  return FILES.map((item) => ({
    ...item,
    bytes: statSync(path.join(releasesDir(), item.file)).size,
  }));
}

export function formatBytes(bytes: number) {
  const mb = bytes / 1_000_000;
  return `${mb.toFixed(1)} MB`;
}
