(ns kur.blog.policy
  "My own polices"
  (:require [clojure.string :as str]
            [kur.blog.page.post.name :as name]))

(def ^:private file-s:title-s
  {"=q=" "?"
   "=star=" "*"})
(defn normalize-title
  "Return title that specific substring replaced. 
   Android doesn't support specific charcters for file name. ex) ? \"
   So to write title including question mark, use alternative str: =q=
   This fn replace alternative to the intended"
  [title]
  (reduce (fn [title [file-str title-str]]
            (str/replace title file-str title-str))
          title file-s:title-s))

(def authors #{"kur"})
(def admin "admin")
(defn admin-post? [post]
  (= admin (-> post :id name/id-info :author)))

(comment
  (normalize-title "=q=eifjsldf=q= =star=asdw =star=")
  ;(str/replace "=q==q==q=" "=q=" "?")
  (admin-post? {:meta-str "+",
                :title "KUR",
                :id "admin0000000000",
                :md-path "/www/blog-base/md/admin0000000000.+.KUR.md",
                :last-modified-millis 1676940818424,
                :frontmatter nil}))