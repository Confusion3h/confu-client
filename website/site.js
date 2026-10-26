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
      "cta.download": "Download for Windows 10/11 (x64)",
      "cta.all": "All platforms",
      "alt.mark": "Confu Client mark",
      "hero.ver": "v0.2.0 · Minecraft 1.8.9 + 1.20.1 (Fabric) · GPL-3.0",
      "stat.fps": "more FPS than the official launcher (est.)",
      "stat.scene": "only native client for Windows on ARM",
      "stat.fps.n": "up to ~50%",
      "stat.scene.n": "ARM64",
      "stat.builds": "builds",
      "features.title": "Features",
      "features.sub": "The same client the launcher starts. FPS mode is already on. Worlds, capes and music stay in your own folder.",
      "feat.fps.title": "FPS boost preset",
      "feat.fps.body": "On the first launch, with FPS mode already on: fast graphics, short render distance, VBOs, fewer particles, no entity shadows, vsync off. Four extra JVM flags sit on top of the existing G1 set. Estimated gain: <span class=\"num\">up to ~50% more FPS than the official Minecraft Launcher (1.8.9)</span>, <span class=\"num\">in line with Lunar, Badlion and Feather</span>. Chunk delay, bot removal and entity culling stay off, so real players stay on screen.",
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
      "feat.java.body": "Every archive ships a JRE. Windows x64, Linux and macOS Intel use Temurin 8. macOS Apple Silicon uses Azul Zulu 8. Windows ARM64 uses a native Microsoft Build of OpenJDK 17, with a small compatibility jar so 1.8.9 still starts. You do not install Java yourself. Confu Client is the only native client for Windows on ARM: no x64 emulation.",
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
      "dl.sub": "Version 0.2.0. New: Minecraft 1.20.1 is now supported (pick 1.20.1 (Modern) in the launcher; Fabric installs on first run). Sizes are the file on disk. SHA-256 is the whole archive.",
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
      "sz.winx64": "60,210,135 bytes<br>60.2 MB",
      "sz.winarm": "83,230,319 bytes<br>83.2 MB",
      "sz.linux64": "74,139,334 bytes<br>74.1 MB",
      "sz.linuxarm": "86,880,287 bytes<br>86.9 MB",
      "sz.mac64": "72,705,581 bytes<br>72.7 MB",
      "sz.macarm": "72,802,191 bytes<br>72.8 MB",
      "source.title": "Source code",
      "source.body": "Confu Client is open source under GPL-3.0. It is a fork of Glide Client, which is based on Soar Client by EldoDebug.",
      "github": "View source on GitHub",
      "faq.title": "FAQ",
      "faq.smart.q": "Windows says the installer is an unrecognized app",
      "faq.smart.a": "SmartScreen shows “Windows protected your PC” because this build is unsigned. There is no Authenticode certificate on the file, so Windows cannot name a publisher it already trusts. The installer still carries a version resource (product and company name Confu Client), a DPI-aware manifest, and LZMA compression. It is not packed with UPX. On the warning, choose <strong>More info</strong>, then <strong>Run anyway</strong>.",
      "faq.which.q": "Which file is mine?",
      "faq.which.a": "The Download button matches Windows, macOS or Linux, and x64 or ARM64, from the browser. Windows ARM64 is the native Java 17 build. The Windows x64 installer also runs on ARM Windows, under emulation, and says so when it starts. Linux and macOS archives unpack and run <code>confu</code> or the app bundle. They are not .exe installers.",
      "faq.java.q": "Do I install Java first?",
      "faq.java.a": "No. Each archive includes a JRE. The game is Minecraft 1.8.9. First launch downloads the game assets into the Confu folder (<code>%APPDATA%\\.confu</code> on Windows, <code>~/.confu</code> on Linux and macOS).",
      "faq.mac.q": "macOS says the app is damaged or can’t be opened",
      "faq.mac.a": "The app is not notarized by Apple yet. Right-click <b>Confu Client</b> and choose <b>Open</b>, then confirm. Or run this in Terminal: <code>xattr -dr com.apple.quarantine \"/path/Confu Client.app\"</code>",
      "faq.fps.q": "Where does the FPS estimate come from?",
      "faq.fps.a": "It is an estimate, not a benchmark of ours. Public 1.8.9 tests usually show OptiFine-based and PvP clients 25–70% above vanilla, depending on the PC. Confu Client stacks the Soar/Glide base, the FPS preset and tuned JVM flags on top, so we put it at up to ~50% over the official launcher and in line with the other PvP clients. Your hardware decides the real number.",
      "footer": "Confu Client 0.2.0 · GPL-3.0 · based on Glide Client and Soar Client by EldoDebug"
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
      "cta.download": "Scarica per Windows 10/11 (x64)",
      "cta.all": "Tutte le piattaforme",
      "alt.mark": "Marchio di Confu Client",
      "hero.ver": "v0.2.0 · Minecraft 1.8.9 + 1.20.1 (Fabric) · GPL-3.0",
      "stat.fps": "FPS in più del launcher ufficiale (stima)",
      "stat.scene": "l’unico client nativo per Windows on ARM",
      "stat.fps.n": "fino a ~50%",
      "stat.scene.n": "ARM64",
      "stat.builds": "build",
      "features.title": "Funzioni",
      "features.sub": "Lo stesso client che avvia il launcher. La modalità FPS è già accesa. Mondi, cappe e musica restano nella tua cartella.",
      "feat.fps.title": "Preset per alzare gli FPS",
      "feat.fps.body": "Al primo avvio, con la modalità FPS già attiva: grafica veloce, distanza di rendering corta, VBO, meno particelle, niente ombre delle entità, vsync spento. Quattro flag JVM in più si aggiungono al set G1 già presente. Guadagno stimato: <span class=\"num\">fino a ~50% di FPS in più rispetto al Minecraft Launcher ufficiale (1.8.9)</span>, <span class=\"num\">in linea con Lunar, Badlion e Feather</span>. Il ritardo dei chunk, la rimozione dei bot e il culling delle entità restano spenti, così i giocatori veri restano in scena.",
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
      "feat.java.body": "Ogni archivio include un JRE. Windows x64, Linux e macOS Intel usano Temurin 8. macOS Apple Silicon usa Azul Zulu 8. Windows ARM64 usa Microsoft Build of OpenJDK 17 nativo, con un piccolo jar di compatibilità così 1.8.9 parte lo stesso. Java non va installato a parte. Confu Client è l’unico client nativo per Windows on ARM: niente emulazione x64.",
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
      "dl.sub": "Versione 0.2.0. Novità: ora è supportato anche Minecraft 1.20.1 (scegli 1.20.1 (Modern) nel launcher; Fabric si installa al primo avvio). Le dimensioni sono quelle del file su disco. Lo SHA-256 è dell’archivio intero.",
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
      "sz.winx64": "60.210.135 byte<br>60,2 MB",
      "sz.winarm": "83.230.319 byte<br>83,2 MB",
      "sz.linux64": "74.139.334 byte<br>74,1 MB",
      "sz.linuxarm": "86.880.287 byte<br>86,9 MB",
      "sz.mac64": "72.705.581 byte<br>72,7 MB",
      "sz.macarm": "72.802.191 byte<br>72,8 MB",
      "source.title": "Codice sorgente",
      "source.body": "Confu Client è pubblicato sotto GPL-3.0. Deriva da Glide Client, a sua volta basato su Soar Client di EldoDebug.",
      "github": "Codice sorgente su GitHub",
      "faq.title": "Domande",
      "faq.smart.q": "Windows segnala l’installer come app non riconosciuta",
      "faq.smart.a": "SmartScreen mostra «Windows ha protetto il PC» perché questa build non è firmata. Sul file non c’è un certificato Authenticode, quindi Windows non può indicare un editore di cui si fida già. L’installer ha comunque una risorsa di versione (prodotto e società Confu Client), un manifest adattato al DPI e la compressione LZMA. Non è compresso con UPX. Nell’avviso scegli <strong>Ulteriori informazioni</strong>, poi <strong>Esegui comunque</strong>.",
      "faq.which.q": "Quale file è il mio?",
      "faq.which.a": "Il pulsante Scarica sceglie Windows, macOS o Linux, e x64 o ARM64, dal browser. Windows ARM64 è la build nativa con Java 17. L’installer Windows x64 gira anche su Windows ARM, in emulazione, e lo dice all’avvio. Gli archivi Linux e macOS si scompattano e avviano <code>confu</code> o il bundle dell’app. Non sono installer .exe.",
      "faq.java.q": "Devo installare Java prima?",
      "faq.java.a": "No. Ogni archivio include un JRE. Il gioco è Minecraft 1.8.9. Al primo avvio gli asset del gioco finiscono nella cartella di Confu (<code>%APPDATA%\\.confu</code> su Windows, <code>~/.confu</code> su Linux e macOS).",
      "faq.mac.q": "macOS dice che l’app è danneggiata o non può essere aperta",
      "faq.mac.a": "L’app non è ancora notarizzata da Apple. Fai clic destro su <b>Confu Client</b> e scegli <b>Apri</b>, poi conferma. Oppure esegui nel Terminale: <code>xattr -dr com.apple.quarantine \"/path/Confu Client.app\"</code>",
      "faq.fps.q": "Da dove arriva la stima degli FPS?",
      "faq.fps.a": "È una stima, non un nostro benchmark. I test pubblici su 1.8.9 mostrano di solito i client basati su OptiFine e i client PvP tra il 25% e il 70% sopra il vanilla, a seconda del PC. Confu Client aggiunge la base Soar/Glide, il preset FPS e flag JVM ottimizzati, quindi lo stimiamo fino a ~50% sopra il launcher ufficiale e in linea con gli altri client PvP. Il numero reale dipende dal tuo hardware.",
      "footer": "Confu Client 0.2.0 · GPL-3.0 · basato su Glide Client e su Soar Client di EldoDebug"
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

  // Main button always offers the Windows x64 installer; other platforms are in the downloads table.
  currentKey = "win-x64";
  setLang(preferred(), false);

})();
