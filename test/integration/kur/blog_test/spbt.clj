(ns integration.kur.blog-test.spbt
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as g]
   [kur.blog.page.post.name :as name]
   [kur.blog.obsidian.tag :as tag]
   [kur.blog.obsidian.frontmatter-test :as fmt]
   [kur.util.file-system :as uf]
   [kur.util.generator :refer [string-from-regexes]]
   [kur.util.regex :refer [ascii* common-whitespace* hangul*]]))

(def gen-md-text
  (string-from-regexes ascii* common-whitespace* hangul*))


(def gen-invalid-post-name
  (g/such-that #(and (s/valid? ::uf/file-name %) (not (name/valid? %)))
               name/gen-post-title))
(def gen-valid-post-name
  (g/fmap name/parts->fname (s/gen ::name/file-name-parts)))
(def gen-post-name (g/one-of [gen-invalid-post-name gen-valid-post-name]))

(def gen-tags-and-frontmatter
  (g/let [tags (g/one-of [g/any-printable-equatable
                          (g/vector (s/gen ::tag/tag))])
          frontmatter (fmt/gen-frontmatter-str
                       (g/one-of [fmt/gen-non-yaml
                                  fmt/gen-no-tags-yaml
                                  (fmt/gen-tags-yaml tags)]))]
    {:tags tags
     :frontmatter frontmatter}))

(comment
  (g/sample gen-md-text)
  (g/sample gen-invalid-post-name)
  (g/sample gen-valid-post-name)
  (g/sample gen-post-name)
  (g/sample gen-tags-and-frontmatter))