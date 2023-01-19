(ns kur.blog-test.spbt.data-generator
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as g]
            [kur.blog.obsidian.frontmatter-test :as fmt]
            [kur.blog.page.post.name :as name]
            [kur.util.file-system :as uf]
            [kur.util.generator :refer [string-from-regexes]]
            [kur.util.regex :refer [ascii* common-whitespace* hangul*]]))

(def gen-invalid-post-fname
  (g/such-that #(and (s/valid? ::uf/file-name %) (not (name/valid? %)))
               name/gen-post-title))
(def gen-valid-post-fname
  (g/such-that #(s/valid? ::uf/file-name %)
               (g/fmap name/parts->fname (s/gen ::name/file-name-parts))))
(def gen-post-name (g/one-of [gen-invalid-post-fname gen-valid-post-fname]))

(def gen-md-text
  (string-from-regexes ascii* common-whitespace* hangul*))
(defn gen-frontmatter [tag-set]
  (g/let [tags (g/vector (g/elements tag-set))]
    (fmt/gen-frontmatter-str
     (g/frequency [[1 fmt/gen-non-yaml]
                   [1 fmt/gen-no-tags-yaml]
                   [8 (fmt/gen-tags-yaml tags)]]))))

(defn gen-md-file [dir tag-set]
  (g/let [fname gen-post-name
          frontmatter (gen-frontmatter tag-set)
          md-text gen-md-text]
    {:path (str (fs/path dir fname)) :text (str frontmatter md-text)}))

(comment
  (g/sample gen-md-text)
  (g/sample gen-invalid-post-fname)
  (g/sample gen-valid-post-fname)
  (g/sample gen-post-name)
  (def tags #{"a" "b" "c" "d"})
  (g/sample (gen-frontmatter tags))
  (g/sample (g/set (gen-md-file "test/fixture/spbt/md/"
                                #{"a" "b" "c/c" "d/e/f"})))
  (run! (fn [{:keys [path text]}] (spit path text))
        (-> "test/fixture/spbt/md/"
            (gen-md-file #{"a" "b" "c/c" "d/e/f"}) g/set g/sample last))

  (def md-files (last (g/sample (g/set (gen-md-file "test/fixture/spbt/md/"
                                                    #{"a" "b" "c/c" "d/e/f"}))))))