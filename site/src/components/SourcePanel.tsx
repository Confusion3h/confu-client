"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";

const TOAST = "Source code coming soon on GitHub";

export function SourcePanel() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (!visible) {
      return;
    }
    const timer = window.setTimeout(() => setVisible(false), 4200);
    return () => window.clearTimeout(timer);
  }, [visible]);

  return (
    <aside className="flex h-full flex-col rounded-2xl border border-white/10 bg-[#14131c] p-5">
      <p className="text-xs font-medium uppercase tracking-[0.16em] text-[#9A96B0]">
        Source code
      </p>
      <h2 className="mt-3 text-xl font-semibold tracking-tight">GitHub</h2>
      <p className="mt-2 text-sm leading-6 text-[#9A96B0]">
        The public repository is not up yet. The button stays on this page.
      </p>
      <Button
        onClick={() => setVisible(true)}
        aria-expanded={visible}
        aria-controls="source-toast"
        className="mt-6 h-11 bg-white text-[#111111] hover:bg-[#E7E5F2]"
      >
        <GitHubMark />
        GitHub
      </Button>
      <p
        id="source-toast"
        role="status"
        aria-live="polite"
        className={`mt-3 rounded-xl border px-3 py-2 text-sm leading-5 transition-opacity ${
          visible
            ? "border-[#FFB547]/50 bg-[#FFB547]/10 text-[#F2F0FA] opacity-100"
            : "pointer-events-none border-transparent opacity-0"
        }`}
      >
        {visible ? TOAST : ""}
      </p>
    </aside>
  );
}

function GitHubMark() {
  return (
    <svg viewBox="0 0 16 16" aria-hidden="true" className="h-4 w-4">
      <path
        fill="currentColor"
        d="M8 0C3.58 0 0 3.58 0 8a8 8 0 0 0 5.47 7.59c.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82a7.7 7.7 0 0 1 2-.27c.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8z"
      />
    </svg>
  );
}
