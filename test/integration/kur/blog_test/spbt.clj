(ns integration.kur.blog-test.spbt
  (:require
   [babashka.fs :as fs]
   [clojure.spec.alpha :as s]
   [clojure.test.check.generators :as g]
   [kur.blog.page.post.name :as name]
   [kur.blog.obsidian.tag :as tag]
   [kur.blog.obsidian.frontmatter-test :as fmt]
   [kur.util.file-system :as uf]
   [kur.util.generator :refer [string-from-regexes]]
   [kur.util.regex :refer [ascii* common-whitespace* hangul*]]))

;; md file
(def gen-invalid-post-name
  (g/such-that #(and (s/valid? ::uf/file-name %) (not (name/valid? %)))
               name/gen-post-title))
(def gen-valid-post-name
  (g/fmap name/parts->fname (s/gen ::name/file-name-parts)))
(def gen-post-name (g/one-of [gen-invalid-post-name gen-valid-post-name]))

(def gen-md-text
  (string-from-regexes ascii* common-whitespace* hangul*))
(def gen-tags-and-frontmatter
  (g/let [tags (g/one-of [g/any-printable-equatable
                          (g/vector (s/gen ::tag/tag))])
          frontmatter (fmt/gen-frontmatter-str
                       (g/one-of [fmt/gen-non-yaml
                                  fmt/gen-no-tags-yaml
                                  (fmt/gen-tags-yaml tags)]))]
    {:tags tags :frontmatter frontmatter}))

(defn gen-md-files [dir]
  (g/let [fname gen-post-name
          {:keys [tags frontmatter]} gen-tags-and-frontmatter
          md-text gen-md-text]
    {:path (str (fs/path dir fname)) :text (str frontmatter md-text)}))

;; ops
(defn gen-create [md-files]
  (g/fmap #(assoc % :kind :create) (g/elements md-files)))
(defn gen-delete [md-files]
  (g/fmap #(hash-map :kind :delete :path (:path %))
          (g/elements md-files)))
#_(defn gen-update [md-files] ;; TODO: update file
    (g/fmap #(assoc % :kind :delete) (g/elements md-files)))
#_(defn gen-test [md-files] ;; test props
    (g/fmap #(assoc % :kind read) (g/elements md-files)))
(def gen-update-sys ;; NOTE: with timing에서는 제거해야..
  (g/return {:kind :upd-sys}))
(defn gen-ops [md-files]
  (g/vector (g/one-of [(gen-create md-files) (gen-delete md-files)
                       gen-update-sys])))

(comment
  (g/sample gen-md-text)
  (g/sample gen-invalid-post-name)
  (g/sample gen-valid-post-name)
  (g/sample gen-post-name)
  (g/sample gen-tags-and-frontmatter)
  (g/sample (g/set (gen-md-files "test/fixture/spbt/md/")))
  (run! (fn [{:keys [path text]}] (spit path text))
        (-> "test/fixture/spbt/md/"
            gen-md-files g/set g/sample last))

  (def md-files (last (g/sample (g/set (gen-md-files "test/fixture/spbt/md/")))))
  (g/sample (gen-create md-files))
  (g/sample (gen-ops md-files)))