(ns kur.blog.policy
  "My own polices"
  (:require [clojure.string :as str]
            [kur.blog.page.post.name :as name]))

(def ^:private file-s:title-s
  {"={=" "["
   "=}=" "]"

   "=(=" "<"
   "=)=" ">"

   "=q=" "?"
   "=;=" ":"
   "=,=" "/"

   "=8=" "*"})

(defn normalize-title
  "Return title that specific substring replaced.
   Android and Obsidian don't support specific charcters for file name.
   ex) ? \" [ ] *
   This fn replace alternative to the intended"
  [title]
  (reduce (fn [title [file-str title-str]]
            (str/replace title file-str title-str))
          title file-s:title-s))

;;
(def authors #{"kur"})
(def admin "admin")
(defn admin-post? [post]
  (= admin (-> post :id name/id-info :author)))

(def introduction-id "admin0000000000")
(def id-404          "admin0000000404")
(def id-50x          "admin0000000500")
(defn introduction-post? [post] (= introduction-id (:id post)))
(defn id-404-post? [post] (= id-404 (:id post)))
(defn id-50x-post? [post] (= id-50x (:id post)))

;;
(def site-resource-dir "resource/site")
(defn site-resource
  "fname is (fs/file-name path): including extension."
  [fname]
  (str site-resource-dir "/" fname))

(def kur-logo (site-resource "kur120.png"))
(def search-logo (site-resource "search.png"))
(def favicon (site-resource "k.png"))

;;
(def layout-css (site-resource "css/layout.css"))
(def header-css (site-resource "css/header.css"))
(def article-css (site-resource "css/article.css"))
(def anchor-css (site-resource "css/anchor.css"))
(def highlight-fragment-css (site-resource "css/highlight_fragment.css"))
(def common-css-paths
  [layout-css header-css article-css
   anchor-css highlight-fragment-css])

(def agate-code-style-link
  "Search `agate` in https://cdnjs.com/libraries/highlight.js"
  [:link {:rel "stylesheet"
          :href "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/styles/agate.min.css"
          :integrity "sha512-wI7oXtzNHj/bqfLA3P6x3XYbcwzsnIKaPLfjjX8ZAXhc65+kSI6sh8gLOOByOKImokAjHUQR0xAJQ/xZTzwuOA=="
          :crossorigin "anonymous"
          :referrerpolicy "no-referrer"}])

;;
(comment
  (normalize-title "=q=eifjsldf=q= =star=asdw =star=")
  ;(str/replace "=q==q==q=" "=q=" "?")
  (admin-post? {:meta-str "+",
                :title "KUR",
                :id "admin0000000000",
                :md-path "/www/blog-base/md/admin0000000000.+.KUR.md",
                :last-modified-millis 1676940818424,
                :frontmatter nil}))
