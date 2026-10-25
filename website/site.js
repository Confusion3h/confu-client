(function () {
  var button = document.querySelector("[data-download]");
  var note = document.querySelector("[data-download-note]");
  var cards = Array.prototype.slice.call(document.querySelectorAll("[data-os]"));
  if (!button || !cards.length) return;

  function labelFor(card) {
    return card.getAttribute("data-label") + " · " + card.getAttribute("data-file");
  }

  function fileHref(card) {
    var link = card.querySelector("a[href]");
    return link ? link.getAttribute("href") : card.getAttribute("href");
  }

  function apply(key) {
    var card = null;
    for (var i = 0; i < cards.length; i++) {
      if (cards[i].getAttribute("data-os") === key) {
        card = cards[i];
        break;
      }
    }
    if (!card) {
      for (var j = 0; j < cards.length; j++) {
        if (cards[j].getAttribute("data-os") === "win-x64") {
          card = cards[j];
          break;
        }
      }
    }
    if (!card) return;
    button.setAttribute("href", fileHref(card));
    if (note) note.textContent = labelFor(card);
    for (var k = 0; k < cards.length; k++) {
      cards[k].classList.toggle("is-picked", cards[k] === card);
    }
  }

  var ua = navigator.userAgent || "";
  var plat = (navigator.userAgentData && navigator.userAgentData.platform) || navigator.platform || "";
  var os = "linux";
  if (/Win/i.test(plat) || /Windows/i.test(ua)) os = "win";
  else if (/Mac/i.test(plat) || /Mac OS X/i.test(ua)) os = "mac";
  var arch = /aarch64|arm64|ARM/i.test(ua) ? "arm64" : "x64";
  apply(os + "-" + arch);

  if (navigator.userAgentData && navigator.userAgentData.getHighEntropyValues) {
    navigator.userAgentData.getHighEntropyValues(["architecture", "platform"]).then(function (hints) {
      var next = os;
      var p = hints.platform || "";
      if (/Windows/i.test(p)) next = "win";
      else if (/mac/i.test(p)) next = "mac";
      else if (/Linux|Chrome OS/i.test(p)) next = "linux";
      var nextArch = /arm/i.test(hints.architecture || "") ? "arm64" : arch;
      apply(next + "-" + nextArch);
    }).catch(function () {});
  }
})();
