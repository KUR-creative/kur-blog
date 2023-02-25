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
(defn introduction-post? [post]
  (= introduction-id (:id post)))

;;
(def site-resource-dir "resource/site")
(defn site-resource
  "fname is (fs/file-name path): including extension."
  [fname]
  (str site-resource-dir "/" fname))

(def kur-logo (site-resource "kur120.png"))
(def search-logo (site-resource "search.png"))
(def favicon (site-resource "k.png"))

(def layout-css (site-resource "css/layout.css"))
(def header-css (site-resource "css/header.css"))
(def article-css (site-resource "css/article.css"))
(def common-css-paths [layout-css header-css article-css])

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
