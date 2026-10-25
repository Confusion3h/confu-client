import { createReadStream } from "fs";
import { stat } from "fs/promises";
import path from "path";
import { Readable } from "stream";
import { RELEASE_FILES, releasesDir } from "@/lib/releases";

export async function GET(
  _request: Request,
  context: { params: Promise<{ name: string }> },
) {
  const { name } = await context.params;
  if (!RELEASE_FILES.has(name) || name.includes("/") || name.includes("..")) {
    return new Response("Not found", { status: 404 });
  }

  const file = path.join(releasesDir(), name);
  const info = await stat(file);
  const stream = createReadStream(file);

  return new Response(Readable.toWeb(stream) as ReadableStream, {
    headers: {
      "Content-Type": "application/octet-stream",
      "Content-Length": String(info.size),
      "Content-Disposition": `attachment; filename="${name}"`,
    },
  });
}
