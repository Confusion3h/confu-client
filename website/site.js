(function () {
  var STR = {
    en: {
      "meta": "Confu Client is a Minecraft 1.8.9 client. FPS mode, Microsoft sign-in, a music player, and a bundled Java runtime.",
      "skip": "Skip to content",
      "nav.aria": "Page",
      "lang.aria": "Language",
      "nav.features": "Features",
      "nav.gallery": "Gallery",
      "nav.downloads": "Downloads",
      "nav.faq": "FAQ",
      "kicker": "Minecraft 1.8.9",
      "hero.title": "Ready when you are.",
      "hero.lede": "Confu Client is an FPS-first 1.8.9 client. The launcher picks a bundled Java, signs you into Microsoft or an offline name, and starts the game.",
      "cta.download": "Download",
      "cta.all": "All platforms",
      "alt.mark": "Confu Client mark",
      "hero.ver": "v0.1.0 · Minecraft 1.8.9 · GPL-3.0",
      "stat.fps": "avg FPS after",
      "stat.scene": "same scene",
      "stat.builds": "builds",
      "features.title": "Features",
      "features.sub": "The same client the launcher starts. FPS mode is already on. Worlds, capes and music stay in your own folder.",
      "feat.fps.title": "FPS boost preset",
      "feat.fps.body": "On the first launch, with FPS mode already on: fast graphics, short render distance, VBOs, fewer particles, no entity shadows, vsync off. Four extra JVM flags sit on top of the existing G1 set. On the same superflat, 12 samples, llvmpipe software GL: <span class=\"num\">78 fps average before (71–85)</span>, <span class=\"num\">81 after (79–85)</span>. That is the safe ceiling. Chunk delay, bot removal and entity culling stay off, so real players stay on screen. A GPU will sit higher than this software-GL pass.",
      "feat.ms.title": "Microsoft login",
      "feat.ms.body": "Sign in with Microsoft from the launcher. It uses the built-in Live desktop client. There is no Azure app to register and no extra token file to paste.",
      "feat.music.title": "Music player",
      "feat.music.body": "Play a folder of WAV, MP3 or OGG, or paste a YouTube or SoundCloud link. yt-dlp and FFmpeg download themselves and are checked against a pinned SHA-256 before they run.",
      "feat.capes.title": "Custom capes",
      "feat.capes.body": "Cosmetics, then Custom. Add a PNG and press F5 to see it. Capes stay on this machine.",
      "feat.esc.title": "ESC menu",
      "feat.esc.body": "Escape in a world opens the vanilla Options screen, including Open Confu Client menu. Right Shift opens that menu from anywhere, including the title screen.",
      "feat.sp.title": "Singleplayer",
      "feat.sp.body": "The 1.8.9 singleplayer list, worlds and all. The main menu is Singleplayer, Multiplayer and Settings. Disconnect returns you there without dropping the process.",
      "feat.java.title": "Java 8, Minecraft 1.8.9",
      "feat.java.body": "Every archive ships a JRE. Windows x64, Linux and macOS Intel use Temurin 8. macOS Apple Silicon uses Azul Zulu 8. Windows ARM64 uses a native Microsoft Build of OpenJDK 17, with a small compatibility jar so 1.8.9 still starts. You do not install Java yourself.",
      "gallery.title": "Gallery",
      "gallery.sub": "Captured from the launcher and the game running on this build. Clips are muted and loop.",
      "cap.launcher": "Launcher home. The light wash is the live window, not a mock.",
      "cap.ms": "Microsoft tab. Sign in with Microsoft, no app setup.",
      "alt.ms": "Confu Client launcher with Sign in with Microsoft",
      "cap.menuvideo": "Main menu. The background follows the pointer.",
      "cap.menu": "Singleplayer, Multiplayer, Settings.",
      "alt.menu": "Confu Client main menu with Singleplayer, Multiplayer and Settings",
      "cap.world": "In a world, FPS display on.",
      "cap.options": "Escape opens Options, with Open Confu Client menu.",
      "alt.options": "Vanilla Options with Open Confu Client menu",
      "cap.client": "Right Shift. FPS mode, FPS display, music.",
      "alt.client": "Confu Client menu with FPS mode and Music player toggles",
      "cap.music": "Music Player. The description sits on its own line, clear of the settings gear.",
      "alt.music": "Music Player module row with the description on its own line",
      "dl.title": "Downloads",
      "dl.sub": "Version 0.1.0. Sizes are the file on disk. SHA-256 is the whole archive. The links point at <code>releases/</code> beside this page, so the site is served from the repository root.",
      "th.platform": "Platform",
      "th.file": "File",
      "th.size": "Size",
      "th.sha": "SHA-256",
      "os.winx64": "Windows x64",
      "os.winarm": "Windows ARM64",
      "os.linux64": "Linux x64",
      "os.linuxarm": "Linux ARM64",
      "os.mac64": "macOS Intel",
      "os.macarm": "macOS Apple Silicon",
      "sz.winx64": "64,145,868 bytes<br>64.1 MB",
      "sz.winarm": "87,166,146 bytes<br>87.2 MB",
      "sz.linux64": "78,050,580 bytes<br>78.1 MB",
      "sz.linuxarm": "90,776,318 bytes<br>90.8 MB",
      "sz.mac64": "80,368,510 bytes<br>80.4 MB",
      "sz.macarm": "76,691,383 bytes<br>76.7 MB",
      "source.title": "Source code",
      "source.body": "Confu Client is open source under GPL-3.0. It is a fork of Glide Client, which is based on Soar Client by EldoDebug.",
      "github": "GitHub",
      "toast": "Source code coming soon on GitHub",
      "faq.title": "FAQ",
      "faq.smart.q": "Windows says the installer is an unrecognized app",
      "faq.smart.a": "SmartScreen shows “Windows protected your PC” because this build is unsigned. There is no Authenticode certificate on the file, so Windows cannot name a publisher it already trusts. The installer still carries a version resource (product and company name Confu Client), a DPI-aware manifest, and LZMA compression. It is not packed with UPX. On the warning, choose <strong>More info</strong>, then <strong>Run anyway</strong>.",
      "faq.which.q": "Which file is mine?",
      "faq.which.a": "The Download button matches Windows, macOS or Linux, and x64 or ARM64, from the browser. Windows ARM64 is the native Java 17 build. The Windows x64 installer also runs on ARM Windows, under emulation, and says so when it starts. Linux and macOS archives unpack and run <code>confu</code> or the app bundle. They are not .exe installers.",
      "faq.java.q": "Do I install Java first?",
      "faq.java.a": "No. Each archive includes a JRE. The game is Minecraft 1.8.9. First launch downloads the game assets into the Confu folder (<code>%APPDATA%\\.confu</code> on Windows, <code>~/.confu</code> on Linux and macOS).",
      "faq.fps.q": "Where is the FPS number from?",
      "faq.fps.a": "One superflat world, 12 samples, before and after the four JVM flags, on llvmpipe (Mesa software OpenGL), not a discrete GPU. Average 78 before, 81 after. The preset was already at the safe graphics ceiling, so the gain is small and honest.",
      "footer": "Confu Client 0.1.0 · GPL-3.0 · based on Glide Client and Soar Client by EldoDebug"
    },
    it: {
      "meta": "Confu Client è un client Minecraft 1.8.9. Modalità FPS, accesso Microsoft, lettore musicale e Java incluso.",
      "skip": "Vai al contenuto",
      "nav.aria": "Pagina",
      "lang.aria": "Lingua",
      "nav.features": "Funzioni",
      "nav.gallery": "Galleria",
      "nav.downloads": "Scaricamenti",
      "nav.faq": "Domande",
      "kicker": "Minecraft 1.8.9",
      "hero.title": "Pronti quando vuoi.",
      "hero.lede": "Confu Client è un client 1.8.9 pensato per gli FPS. Il launcher sceglie il Java incluso, entra con Microsoft o con un nome offline e avvia il gioco.",
      "cta.download": "Scarica",
      "cta.all": "Tutte le piattaforme",
      "alt.mark": "Marchio di Confu Client",
      "hero.ver": "v0.1.0 · Minecraft 1.8.9 · GPL-3.0",
      "stat.fps": "FPS medi dopo",
      "stat.scene": "stessa scena",
      "stat.builds": "build",
      "features.title": "Funzioni",
      "features.sub": "Lo stesso client che avvia il launcher. La modalità FPS è già accesa. Mondi, cappe e musica restano nella tua cartella.",
      "feat.fps.title": "Preset per alzare gli FPS",
      "feat.fps.body": "Al primo avvio, con la modalità FPS già attiva: grafica veloce, distanza di rendering corta, VBO, meno particelle, niente ombre delle entità, vsync spento. Quattro flag JVM in più si aggiungono al set G1 già presente. Stessa superflat, 12 campioni, GL software llvmpipe: <span class=\"num\">78 fps di media prima (71–85)</span>, <span class=\"num\">81 dopo (79–85)</span>. È il tetto sicuro. Il ritardo dei chunk, la rimozione dei bot e il culling delle entità restano spenti, così i giocatori veri restano in scena. Con una GPU il numero sale rispetto a questo passaggio in GL software.",
      "feat.ms.title": "Accesso Microsoft",
      "feat.ms.body": "Entra con Microsoft dal launcher. Usa il client Live desktop già incluso. Non c’è un’app Azure da registrare e nessun file di token da incollare.",
      "feat.music.title": "Lettore musicale",
      "feat.music.body": "Riproduci una cartella di WAV, MP3 o OGG, oppure incolla un link di YouTube o SoundCloud. yt-dlp e FFmpeg si scaricano da soli e vengono controllati con uno SHA-256 fissato prima di partire.",
      "feat.capes.title": "Cappe personalizzate",
      "feat.capes.body": "Cosmetici, poi Personalizzata. Aggiungi un PNG e premi F5 per vederla. Le cappe restano su questo computer.",
      "feat.esc.title": "Menu con Esc",
      "feat.esc.body": "Esc in un mondo apre la schermata Opzioni vanilla, compreso il pulsante per aprire il menu di Confu Client. Il Maiusc destro apre quel menu da qualsiasi schermata, titolo compreso.",
      "feat.sp.title": "Giocatore singolo",
      "feat.sp.body": "L’elenco in singolo di 1.8.9, mondi compresi. Il menu principale ha le voci per il singolo, il multigiocatore e le impostazioni. La disconnessione ti riporta lì senza chiudere il processo.",
      "feat.java.title": "Java 8 e Minecraft 1.8.9",
      "feat.java.body": "Ogni archivio include un JRE. Windows x64, Linux e macOS Intel usano Temurin 8. macOS Apple Silicon usa Azul Zulu 8. Windows ARM64 usa Microsoft Build of OpenJDK 17 nativo, con un piccolo jar di compatibilità così 1.8.9 parte lo stesso. Java non va installato a parte.",
      "gallery.title": "Galleria",
      "gallery.sub": "Catturate dal launcher e dal gioco avviati con questa build. Le clip sono senza audio e in loop.",
      "cap.launcher": "Home del launcher. La scia di luce è la finestra vera, non una simulazione.",
      "cap.ms": "Scheda Microsoft. Accesso con Microsoft, senza configurare un’app.",
      "alt.ms": "Launcher di Confu Client con l’accesso Microsoft",
      "cap.menuvideo": "Menu principale. Lo sfondo segue il puntatore.",
      "cap.menu": "Singolo, multigiocatore, impostazioni.",
      "alt.menu": "Menu principale di Confu Client con singolo, multigiocatore e impostazioni",
      "cap.world": "In un mondo, con il contatore FPS acceso.",
      "cap.options": "Esc apre Opzioni, con la voce per aprire il menu di Confu Client.",
      "alt.options": "Opzioni vanilla con la voce per aprire il menu di Confu Client",
      "cap.client": "Maiusc destro. Modalità FPS, contatore FPS, musica.",
      "alt.client": "Menu di Confu Client con gli interruttori di modalità FPS e lettore musicale",
      "cap.music": "Lettore musicale. La descrizione sta sulla sua riga, lontana dall’ingranaggio.",
      "alt.music": "Riga del lettore musicale con la descrizione sulla propria riga",
      "dl.title": "Scaricamenti",
      "dl.sub": "Versione 0.1.0. Le dimensioni sono quelle del file su disco. Lo SHA-256 è dell’archivio intero. I link puntano a <code>releases/</code> accanto a questa pagina, quindi il sito va servito dalla radice del repository.",
      "th.platform": "Piattaforma",
      "th.file": "File",
      "th.size": "Dimensione",
      "th.sha": "SHA-256",
      "os.winx64": "Windows x64",
      "os.winarm": "Windows ARM64",
      "os.linux64": "Linux x64",
      "os.linuxarm": "Linux ARM64",
      "os.mac64": "macOS Intel",
      "os.macarm": "macOS Apple Silicon",
      "sz.winx64": "64.145.868 byte<br>64,1 MB",
      "sz.winarm": "87.166.146 byte<br>87,2 MB",
      "sz.linux64": "78.050.580 byte<br>78,1 MB",
      "sz.linuxarm": "90.776.318 byte<br>90,8 MB",
      "sz.mac64": "80.368.510 byte<br>80,4 MB",
      "sz.macarm": "76.691.383 byte<br>76,7 MB",
      "source.title": "Codice sorgente",
      "source.body": "Confu Client è pubblicato sotto GPL-3.0. Deriva da Glide Client, a sua volta basato su Soar Client di EldoDebug.",
      "github": "GitHub",
      "toast": "Codice sorgente in arrivo su GitHub",
      "faq.title": "Domande",
      "faq.smart.q": "Windows segnala l’installer come app non riconosciuta",
      "faq.smart.a": "SmartScreen mostra «Windows ha protetto il PC» perché questa build non è firmata. Sul file non c’è un certificato Authenticode, quindi Windows non può indicare un editore di cui si fida già. L’installer ha comunque una risorsa di versione (prodotto e società Confu Client), un manifest adattato al DPI e la compressione LZMA. Non è compresso con UPX. Nell’avviso scegli <strong>Ulteriori informazioni</strong>, poi <strong>Esegui comunque</strong>.",
      "faq.which.q": "Quale file è il mio?",
      "faq.which.a": "Il pulsante Scarica sceglie Windows, macOS o Linux, e x64 o ARM64, dal browser. Windows ARM64 è la build nativa con Java 17. L’installer Windows x64 gira anche su Windows ARM, in emulazione, e lo dice all’avvio. Gli archivi Linux e macOS si scompattano e avviano <code>confu</code> o il bundle dell’app. Non sono installer .exe.",
      "faq.java.q": "Devo installare Java prima?",
      "faq.java.a": "No. Ogni archivio include un JRE. Il gioco è Minecraft 1.8.9. Al primo avvio gli asset del gioco finiscono nella cartella di Confu (<code>%APPDATA%\\.confu</code> su Windows, <code>~/.confu</code> su Linux e macOS).",
      "faq.fps.q": "Da dove arriva il numero degli FPS?",
      "faq.fps.a": "Un solo mondo superflat, 12 campioni, prima e dopo i quattro flag JVM, su llvmpipe (OpenGL software di Mesa), non su una GPU dedicata. Media 78 prima, 81 dopo. Il preset era già al tetto grafico sicuro, quindi il guadagno è piccolo e dichiarato.",
      "footer": "Confu Client 0.1.0 · GPL-3.0 · basato su Glide Client e su Soar Client di EldoDebug"
    }
  };

  var currentKey = "win-x64";
  var button = document.querySelector("[data-download]");
  var note = document.querySelector("[data-download-note]");
  var cards = Array.prototype.slice.call(document.querySelectorAll("[data-os]"));

  function dict(lang) {
    return STR[lang] || STR.en;
  }

  function fill(lang) {
    var pack = dict(lang);
    document.documentElement.lang = lang === "it" ? "it" : "en";
    var meta = document.querySelector('meta[name="description"]');
    if (meta && pack.meta) meta.setAttribute("content", pack.meta);
    var nodes = document.querySelectorAll("[data-i18n]");
    for (var i = 0; i < nodes.length; i++) {
      var el = nodes[i];
      var key = el.getAttribute("data-i18n");
      if (pack[key] == null) continue;
      if (el.hasAttribute("data-i18n-html")) el.innerHTML = pack[key];
      else el.textContent = pack[key];
    }
    var alts = document.querySelectorAll("[data-i18n-alt]");
    for (var a = 0; a < alts.length; a++) {
      var img = alts[a];
      var altKey = img.getAttribute("data-i18n-alt");
      if (pack[altKey] != null) img.alt = pack[altKey];
    }
    var arias = document.querySelectorAll("[data-i18n-aria]");
    for (var r = 0; r < arias.length; r++) {
      var region = arias[r];
      var ariaKey = region.getAttribute("data-i18n-aria");
      if (pack[ariaKey] != null) region.setAttribute("aria-label", pack[ariaKey]);
    }
    var switches = document.querySelectorAll("[data-set-lang]");
    for (var s = 0; s < switches.length; s++) {
      var sw = switches[s];
      var on = sw.getAttribute("data-set-lang") === lang;
      sw.setAttribute("aria-pressed", on ? "true" : "false");
    }
  }

  function fileHref(card) {
    var link = card.querySelector("a[href]");
    return link ? link.getAttribute("href") : "";
  }

  function applyOs(key) {
    var card = null;
    var i;
    for (i = 0; i < cards.length; i++) {
      if (cards[i].getAttribute("data-os") === key) {
        card = cards[i];
        break;
      }
    }
    if (!card) {
      for (i = 0; i < cards.length; i++) {
        if (cards[i].getAttribute("data-os") === "win-x64") {
          card = cards[i];
          break;
        }
      }
    }
    if (!card || !button) return;
    currentKey = card.getAttribute("data-os");
    button.setAttribute("href", fileHref(card));
    if (note) {
      var labelEl = card.querySelector("[data-i18n]");
      var label = labelEl ? labelEl.textContent : card.getAttribute("data-label");
      note.textContent = label + " · " + card.getAttribute("data-file");
    }
    for (i = 0; i < cards.length; i++) {
      cards[i].classList.toggle("is-picked", cards[i] === card);
    }
  }

  function preferred() {
    try {
      var saved = localStorage.getItem("confu.lang");
      if (saved === "en" || saved === "it") return saved;
    } catch (err) {}
    var navLang = (navigator.language || "").toLowerCase();
    if (navLang.indexOf("it") === 0) return "it";
    return "en";
  }

  function setLang(lang, persist) {
    if (lang !== "en" && lang !== "it") lang = "en";
    fill(lang);
    applyOs(currentKey);
    if (persist) {
      try { localStorage.setItem("confu.lang", lang); } catch (err) {}
    }
  }

  var switches = document.querySelectorAll("[data-set-lang]");
  for (var s = 0; s < switches.length; s++) {
    switches[s].addEventListener("click", function () {
      setLang(this.getAttribute("data-set-lang"), true);
    });
  }

  var source = document.querySelector("[data-source]");
  var toast = document.querySelector("[data-toast]");
  var toastTimer = 0;
  if (source && toast) {
    source.addEventListener("click", function () {
      toast.hidden = false;
      window.clearTimeout(toastTimer);
      toastTimer = window.setTimeout(function () {
        toast.hidden = true;
      }, 4200);
    });
  }

  var ua = navigator.userAgent || "";
  var plat = (navigator.userAgentData && navigator.userAgentData.platform) || navigator.platform || "";
  var os = "linux";
  if (/Win/i.test(plat) || /Windows/i.test(ua)) os = "win";
  else if (/Mac/i.test(plat) || /Mac OS X/i.test(ua)) os = "mac";
  var arch = /aarch64|arm64|ARM/i.test(ua) ? "arm64" : "x64";
  currentKey = os + "-" + arch;
  setLang(preferred(), false);

  if (navigator.userAgentData && navigator.userAgentData.getHighEntropyValues) {
    navigator.userAgentData.getHighEntropyValues(["architecture", "platform"]).then(function (hints) {
      var next = os;
      var p = hints.platform || "";
      if (/Windows/i.test(p)) next = "win";
      else if (/mac/i.test(p)) next = "mac";
      else if (/Linux|Chrome OS/i.test(p)) next = "linux";
      var nextArch = /arm/i.test(hints.architecture || "") ? "arm64" : arch;
      applyOs(next + "-" + nextArch);
    }).catch(function () {});
  }

})();
