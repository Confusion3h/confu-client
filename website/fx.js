(function () {
  "use strict";
  var d = document, root = d.documentElement;
  var reduce = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var finePointer = window.matchMedia("(hover: hover) and (pointer: fine)").matches;

  // intro loader: short, once per session, skipped on reduced motion
  var intro = d.querySelector(".intro");
  if (intro) {
    var seen = false;
    try { seen = sessionStorage.getItem("confu.intro") === "1"; sessionStorage.setItem("confu.intro", "1"); } catch (e) {}
    var hide = function () { intro.classList.add("done"); };
    if (reduce || seen) hide(); else setTimeout(hide, 950);
  }

  // theme toggle
  var themeBtn = d.querySelector("[data-theme-toggle]");
  var meta = d.querySelector('meta[name="theme-color"]');
  function paintTheme() { if (meta) meta.setAttribute("content", root.dataset.theme === "light" ? "#f4f4f2" : "#0a0a0a"); }
  paintTheme();
  if (themeBtn) themeBtn.addEventListener("click", function () {
    var next = root.dataset.theme === "light" ? "dark" : "light";
    root.dataset.theme = next;
    try { localStorage.setItem("confu.theme", next); } catch (e) {}
    paintTheme();
  });

  // mobile menu
  var menuBtn = d.querySelector("[data-menu-toggle]");
  function setMenu(open) {
    root.classList.toggle("menu-open", open);
    if (menuBtn) menuBtn.setAttribute("aria-expanded", open ? "true" : "false");
  }
  if (menuBtn) menuBtn.addEventListener("click", function () { setMenu(!root.classList.contains("menu-open")); });
  d.querySelectorAll("[data-mobile-menu] a").forEach(function (a) { a.addEventListener("click", function () { setMenu(false); }); });
  d.addEventListener("keydown", function (e) { if (e.key === "Escape") setMenu(false); });

  // scroll: nav state + progress bar
  var bar = d.querySelector(".progress span");
  var ticking = false;
  function onScroll() {
    ticking = false;
    var y = window.scrollY, max = root.scrollHeight - window.innerHeight;
    root.classList.toggle("scrolled", y > 24);
    if (bar) bar.style.setProperty("--p", max > 0 ? (y / max).toFixed(4) : 0);
  }
  window.addEventListener("scroll", function () { if (!ticking) { ticking = true; requestAnimationFrame(onScroll); } }, { passive: true });
  onScroll();

  var io = "IntersectionObserver" in window;
  // active nav link
  var links = Array.prototype.slice.call(d.querySelectorAll(".nav-links a[href^='#']"));
  if (io && links.length) {
    var spy = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (!en.isIntersecting) return;
        links.forEach(function (a) { a.classList.toggle("active", a.getAttribute("href") === "#" + en.target.id); });
      });
    }, { rootMargin: "-45% 0px -50% 0px" });
    links.forEach(function (a) { var s = d.querySelector(a.getAttribute("href")); if (s) spy.observe(s); });
    var top = d.getElementById("top"); if (top) spy.observe(top);
  }

  // reveal on scroll
  var reveals = d.querySelectorAll(".reveal");
  if (!io || reduce) reveals.forEach(function (el) { el.classList.add("in"); });
  else {
    var rio = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) { if (en.isIntersecting) { en.target.classList.add("in"); rio.unobserve(en.target); } });
    }, { rootMargin: "0px 0px -8% 0px", threshold: 0.08 });
    reveals.forEach(function (el) { rio.observe(el); });
  }

  // click ripple
  d.querySelectorAll(".ripple").forEach(function (b) {
    b.addEventListener("pointerdown", function (e) {
      if (reduce) return;
      var r = b.getBoundingClientRect(), s = d.createElement("span");
      s.className = "ripple-dot";
      s.style.left = (e.clientX - r.left) + "px"; s.style.top = (e.clientY - r.top) + "px";
      b.appendChild(s);
      setTimeout(function () { s.remove(); }, 750);
    });
  });

  if (finePointer && !reduce) {
    // cursor glow
    var glow = d.querySelector(".cursor-glow"), gx = 0, gy = 0, raf = 0;
    window.addEventListener("pointermove", function (e) {
      gx = e.clientX; gy = e.clientY;
      if (!raf) raf = requestAnimationFrame(function () {
        raf = 0; root.classList.add("has-pointer");
        if (glow) { glow.style.setProperty("--cx", gx + "px"); glow.style.setProperty("--cy", gy + "px"); }
      });
    }, { passive: true });
    // card spotlight
    d.querySelectorAll(".spot").forEach(function (c) {
      c.addEventListener("pointermove", function (e) {
        var r = c.getBoundingClientRect();
        c.style.setProperty("--mx", (e.clientX - r.left) + "px");
        c.style.setProperty("--my", (e.clientY - r.top) + "px");
      });
    });
    // magnetic CTA
    d.querySelectorAll(".magnetic").forEach(function (m) {
      m.addEventListener("pointermove", function (e) {
        var r = m.getBoundingClientRect();
        var x = (e.clientX - r.left - r.width / 2) * 0.18, y = (e.clientY - r.top - r.height / 2) * 0.3;
        m.style.transform = "translate(" + x.toFixed(1) + "px," + y.toFixed(1) + "px)";
      });
      m.addEventListener("pointerleave", function () { m.style.transform = ""; });
    });
  }
})();
