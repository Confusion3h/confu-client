import { SourcePanel } from "@/components/SourcePanel";
import { formatBytes, loadReleases } from "@/lib/releases";

const FEATURES = [
  {
    title: "FPS mode",
    body: "On from the first launch. Fast graphics, fewer particles, and a tuned JVM. Players stay visible.",
  },
  {
    title: "Music",
    body: "Play a local folder, paste a YouTube or SoundCloud link, or control Spotify when you want it.",
  },
  {
    title: "Microsoft sign-in",
    body: "The built-in Live sign-in. No app registration on your side.",
  },
  {
    title: "A JRE in the box",
    body: "Windows, macOS and Linux, x64 and ARM64. Each archive carries the runtime for that machine.",
  },
];

export default function Home() {
  const releases = loadReleases();

  return (
    <div className="min-h-screen">
      <header className="mx-auto flex max-w-5xl items-center justify-between px-5 py-6 sm:px-8">
        <a href="#top" className="flex items-center gap-3">
          <img src="/mark.svg" alt="" width={36} height={36} />
          <span className="text-sm font-semibold tracking-tight">Confu Client</span>
        </a>
        <a
          href="#downloads"
          className="rounded-full border border-white/15 px-3 py-1.5 text-sm text-[#F2F0FA] hover:bg-white/5"
        >
          Downloads
        </a>
      </header>

      <main id="top" className="mx-auto max-w-5xl px-5 pb-20 sm:px-8">
        <section className="pt-8 sm:pt-16">
          <p className="text-xs font-medium uppercase tracking-[0.18em] text-[#9A96B0]">
            Minecraft 1.8.9 · version 0.1.0
          </p>
          <h1 className="mt-4 max-w-xl text-4xl font-semibold tracking-tight sm:text-5xl">
            A client that stays out of the frame time.
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-[#9A96B0]">
            Confu Client is the Soar and Glide line, kept for 1.8.9, with FPS mode
            on by default and an installer that brings Java with it.
          </p>
        </section>

        <section id="downloads" className="mt-14 scroll-mt-8">
          <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
            <div className="rounded-2xl border border-white/10 bg-[#14131c] p-5 sm:p-6">
              <h2 className="text-xl font-semibold tracking-tight">Downloads</h2>
              <p className="mt-2 text-sm leading-6 text-[#9A96B0]">
                Pick the build for your machine. Each one includes the game and a JRE.
              </p>
              <ul className="mt-5 divide-y divide-white/10">
                {releases.map((item) => (
                  <li key={item.file}>
                    <a
                      href={`/downloads/${item.file}`}
                      download={item.file}
                      className="group flex flex-col gap-1 py-4 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <span>
                        <span className="block text-sm font-medium text-[#F2F0FA] group-hover:text-white">
                          {item.os}{" "}
                          <span className="font-normal text-[#9A96B0]">{item.arch}</span>
                        </span>
                        <span className="mt-1 block break-all text-xs leading-5 text-[#9A96B0]">
                          {item.detail} · {item.file}
                        </span>
                      </span>
                      <span className="text-sm tabular-nums text-[#F2F0FA] sm:pl-6">
                        {formatBytes(item.bytes)}
                      </span>
                    </a>
                  </li>
                ))}
              </ul>
            </div>
            <SourcePanel />
          </div>
        </section>

        <section className="mt-16">
          <h2 className="text-xl font-semibold tracking-tight">What you get</h2>
          <ul className="mt-5 grid gap-3 sm:grid-cols-2">
            {FEATURES.map((feature) => (
              <li key={feature.title} className="rounded-2xl border border-white/10 p-5">
                <h3 className="text-sm font-semibold">{feature.title}</h3>
                <p className="mt-2 text-sm leading-6 text-[#9A96B0]">{feature.body}</p>
              </li>
            ))}
          </ul>
        </section>
      </main>

      <footer className="border-t border-white/10">
        <div className="mx-auto flex max-w-5xl flex-col gap-3 px-5 py-8 text-sm leading-6 text-[#9A96B0] sm:px-8">
          <p>
            Confu Client is free software under GPL-3.0. It is a fork of{" "}
            <a className="text-[#F2F0FA] underline decoration-white/30 underline-offset-4 hover:decoration-white" href="https://github.com/GlideClient/client">
              Glide Client
            </a>
            , which is based on{" "}
            <a className="text-[#F2F0FA] underline decoration-white/30 underline-offset-4 hover:decoration-white" href="https://github.com/Soar-Client/Legacy-SoarClient">
              Soar Client
            </a>{" "}
            by EldoDebug.
          </p>
          <p>Version 0.1.0</p>
        </div>
      </footer>
    </div>
  );
}
