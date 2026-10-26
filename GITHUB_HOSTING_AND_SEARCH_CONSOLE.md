# 🚀 Gabay sa APK Hosting sa GitHub at Pag-submit sa Google Search Console (Sitemap)

Nakahanda na ang lahat ng kinakailangang files sa repository para sa **100% Live APK Hosting** at **Google Search Console Indexing**:
- 📦 **`app-release.apk`** (Root directory - 24 MB)
- 📦 **`OceanDeepEscape.apk`** (Root directory - 24 MB)
- 🗺️ **`sitemap.xml`** (Naglalaman ng URLs para sa website, direct APK download, at app-ads.txt)
- 🤖 **`robots.txt`** (Nagpapahintulot sa Googlebot at nagtuturo sa `sitemap.xml`)
- 🌐 **`index.html`** (May kasamang Schema.org SoftwareApplication rich snippet, canonical URL, at Open Graph tags)
- ⚙️ **`.github/workflows/deploy.yml`** (Awtomatikong magbi-build at magde-deploy sa GitHub Pages at Releases)

---

## 📌 HAKBANG 1: I-upload ang Repository at APK sa GitHub

1. **Pumunta sa GitHub:**
   - Mag-log in sa [github.com](https://github.com) at gumawa ng bagong public repository (halimbawa: `OceanDeepEscape`).
2. **I-push ang Code sa GitHub:**
   - Gamitin ang Git commands:
     ```bash
     git add .
     git commit -m "Production release: APK hosting, sitemap, and Google Search Console integration"
     git branch -M main
     git remote add origin https://github.com/USERNAME/OceanDeepEscape.git
     git push -u origin main
     ```

---

## 📦 HAKBANG 2: Libre at Mabilis na APK Hosting Gamit ang GitHub Releases

Ang **GitHub Releases** ang opisyal at pinaka-inirerekomendang paraan para mag-host ng Android APK dahil mayroon itong libreng global CDN na walang download bandwidth limits:

1. Sa iyong GitHub repository, i-click ang **Releases** (matatagpuan sa kanang bahagi ng screen) o pumunta sa `https://github.com/USERNAME/OceanDeepEscape/releases`.
2. I-click ang **"Draft a new release"**.
3. Punan ang mga sumusunod:
   - **Choose a tag:** `v1.0.0` (i-click ang *Create new tag: v1.0.0 on publish*)
   - **Release title:** `Ocean Deep Escape v1.0.0 (Official Production APK)`
   - **Description:** Ilagay ang release notes ng laro (500 Abyssal Zones, Apex Sharks, Boss Fights, Unlimited Torpedoes).
4. Sa kahon na may nakasulat na **"Attach binaries by dropping them here or selecting them"**:
   - I-drag at i-drop ang **`app-release.apk`** (o `OceanDeepEscape.apk`).
5. I-click ang **"Publish release"**.
6. **Mababang URL ng Download:**
   - Magkakaroon ka na ng direktang permanent link tulad ng:
     `https://github.com/USERNAME/OceanDeepEscape/releases/download/v1.0.0/app-release.apk`
   - Ang link na ito ay ligtas, mabilis, at hindi nag-e-expire!

---

## 🌐 HAKBANG 3: I-activate ang GitHub Pages (Libreng Website at Sitemap Hosting)

Upang mai-serve ang `sitemap.xml`, `robots.txt`, at ang live web version:

1. Sa iyong GitHub Repository, i-click ang **Settings** > **Pages** (sa kaliwang sidebar).
2. Sa ilalim ng **"Build and deployment"** > **Source**:
   - Piliin ang **"Deploy from a branch"**.
   - **Branch:** Piliin ang `main` (o `master`) at folder `/ (root)`.
   - I-click ang **Save**.
3. Pagkatapos ng 1 hanggang 2 minuto, magiging live ang iyong site sa:
   `https://USERNAME.github.io/OceanDeepEscape/`
4. Maaari mo ring ma-access ang iyong files:
   - Website: `https://USERNAME.github.io/OceanDeepEscape/`
   - Sitemap: `https://USERNAME.github.io/OceanDeepEscape/sitemap.xml`
   - Robots: `https://USERNAME.github.io/OceanDeepEscape/robots.txt`
   - AdMob Verification: `https://USERNAME.github.io/OceanDeepEscape/app-ads.txt`

*(Tandaan: Palitan ang `USERNAME` ng iyong aktwal na GitHub account username).*

---

## 🔍 HAKBANG 4: I-verify ang Property sa Google Search Console

1. Pumunta sa [Google Search Console](https://search.google.com/search-console).
2. Mag-sign in gamit ang iyong Google Account.
3. I-click ang **"Add property"** sa dropdown menu sa itaas sa kaliwa:
   - Piliin ang **URL prefix**.
   - I-type ang buong URL ng iyong GitHub Pages site:
     `https://USERNAME.github.io/OceanDeepEscape/`
   - I-click ang **Continue**.
4. **Paraan ng Pag-verify:**
   - **Opsyon A (HTML Tag - Pinakamadali):**
     1. Piliin ang **HTML tag** verification method.
     2. Kopyahin ang `meta tag` na ibibigay ng Google (halimbawa: `<meta name="google-site-verification" content="ABC123xyz..." />`).
     3. I-paste ang verification code sa `index.html` sa linyang:
        `<meta name="google-site-verification" content="DITO_IPASTE_ANG_TOKEN">`
     4. I-commit at i-push sa GitHub, pagkatapos ay bumalik sa Google Search Console at i-click ang **"Verify"**.
   - **Opsyon B (HTML File Upload):**
     1. I-download ang `.html` verification file mula sa Google Search Console.
     2. Ilagay ito sa root directory ng repo at i-push sa GitHub.
     3. I-click ang **"Verify"**.

---

## 🗺️ HAKBANG 5: I-submit ang Sitemap sa Google Search Console

Kapag verified na ang iyong property:

1. Sa Google Search Console dashboard, i-click ang **Sitemaps** sa kaliwang navigation menu (sa ilalim ng *Indexing*).
2. Sa ilalim ng **"Add a new sitemap"**, makikita mo ang iyong domain:
   `https://USERNAME.github.io/OceanDeepEscape/`
3. Sa katabing text box, i-type lamang ang:
   **`sitemap.xml`**
4. I-click ang **SUBMIT**.
5. **Resulta:**
   - Makikita mo ang status na **"Success"** (kulay berde).
   - Babasahin ng Googlebot ang lahat ng links (homepage, `app-release.apk`, at `app-ads.txt`).
   - Magsisimula nang i-crawl at i-index ng Google Search ang iyong laro upang lumabas ito sa mga search results kapag may naghahanap ng APK download ng *Ocean Deep Escape*!
