(ns integration.kur.blog-test.spbt
  (:require
   [babashka.fs :as fs]
   [clojure.spec.alpha :as s]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as g]
   [clojure.test.check.properties
    :refer [for-all] :rename {for-all defp}]
   [kur.blog.obsidian.frontmatter-test :as fmt]
   [kur.blog.obsidian.tag :as tag]
   [kur.blog.page.post.name :as name]
   [kur.blog.page.post :as post]
   [kur.blog.page.tags :as tags]
   [kur.blog.updater :as updater]
   [kur.util.file-system :as uf]
   [kur.util.generator :refer [string-from-regexes]]
   [kur.util.regex :refer [ascii* common-whitespace* hangul*]]))

;; md file
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
  (def md-files md-files)
  (g/vector (g/one-of [(gen-create md-files)
                       (gen-delete md-files)
                       gen-update-sys])))

;; Runners
(defn next-model [{:keys [path text kind] :as op} model]
  (case kind
    :create  (let [parts (name/fname->parts (fs/file-name path))]
               (if (and (name/valid? (fs/file-name path))
                        (post/public? parts))
                 (assoc model path text)
                 model))
    :delete  (dissoc model path)
    :upd-sys model))

(defn next-actual
  "The actual state is a directory in file system!"
  [op [old-posts md-dir html-dir]]
  (case (:kind op)
    :create  (do (spit (:path op) (:text op)) old-posts)
    :delete  (do (fs/delete-if-exists (:path op)) old-posts)
    :upd-sys (let [new-posts (updater/post-set md-dir)]
               (def old-posts old-posts)
               (def new-posts new-posts)
               (def s (updater/site old-posts new-posts html-dir))
               (updater/update! (updater/site old-posts new-posts html-dir))
               new-posts)))

;; Properties
(defn same-num-public-pages? [model html-dir]
  (when (not= (+ (count model) 2)
              (count (uf/path-seq html-dir #(= (fs/extension %) "html"))))
    (def model model)
    (def fseq (uf/path-seq html-dir #(= (fs/extension %) "html"))))
  (= (+ (count model) 2) ; 2 = tags + home
     (count (uf/path-seq html-dir #(= (fs/extension %) "html")))))

(defn correct-tags-page? [md-dir tags-html-str]
  (def md-dir md-dir)
  (def tags-html-str tags-html-str)
  (every? #(.contains tags-html-str %)
          (keep :id (updater/post-set md-dir))))

;; Test
;(def cnt (atom 0))
(defn teardown-and-return [ret & dirs]
  (run! uf/delete-all-except-gitkeep dirs)
  ret)

(defspec test-without-timing
  {:num-tests 100
   ;:seed 1674007981491
   ;:max-size 60
   }
  (let [md-dir "test/fixture/spbt/md"
        html-dir "test/fixture/spbt/html"
        tags-html-path (str (fs/path html-dir "tags.html"))]
    (defp [[tag-set md-files ops]
           (g/let [tag-set (g/set (s/gen ::tag/tag) {:min-elements 1})
                   md-files (g/set (gen-md-file md-dir tag-set)
                                   {:min-elements 1})
                   ops (gen-ops md-files)]
             [tag-set md-files ops])
           #_(g/return [{:path "test/fixture/spbt/md/A7001010900.+.md", :text "", :kind :create}
                        {:kind :upd-sys}])
           #_(g/return
              [{:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}
               {:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}])
           #_(g/return [{:path "test/fixture/spbt/md/A7001010900.+.md",
                         :text "---\ntags:\n- []\nxs: 0\n\n---", :kind :create}
                        {:kind :upd-sys}])]
      (def ops ops)
      (teardown-and-return nil md-dir html-dir)

      ;(reset! cnt 0) (println "") ;;;
      (loop [posts (updater/post-set md-dir), model {}, ops ops]
        (if-let [op (first ops)]
          (let [;; Update model & actual
                new-model (next-model op model)
                new-posts (next-actual op [posts md-dir html-dir])]
            ;; Check property
            (if (or (not= (:kind op) :upd-sys)
                    ;; Properties
                    (and (same-num-public-pages? new-model html-dir)
                         (correct-tags-page? md-dir
                                             (slurp tags-html-path))))
              (recur new-posts new-model (rest ops))
              (teardown-and-return false md-dir html-dir)))
          (teardown-and-return true md-dir html-dir))) ;; pass test 
      )))

(comment
  (test-without-timing)

  ;;
  (def m0 {})
  (def m1 (next-model {:path "test/fixture/spbt/md/A7001010900.+.md", :text "", :kind :create} m0))
  (def m2 (next-model {:path "test/fixture/spbt/md/|", :text "", :kind :create} m1))
  (def m3 (next-model {:kind :upd-sys} m2))
  #_(def m4 (next-model {:kind :upd-sys} m3))

  (def olds (updater/post-set md-dir))
  (next-actual {:path "test/fixture/spbt/md/A7001010900.md", :text " ", :kind :create} nil)
  (def news (updater/post-set md-dir))
  (next-actual {:kind :upd-sys} [olds news html-dir])

  #_(def olds1 (updater/post-set md-dir))
  #_(next-actual {:path "test/fixture/spbt/md/|", :text "", :kind :create} nil)
  #_(def news1 (updater/post-set md-dir))
  #_(next-actual {:kind :upd-sys} [olds1 news1 html-dir])

  ;;
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
                                                    #{"a" "b" "c/c" "d/e/f"})))))
  (g/sample (gen-create md-files))
  (g/sample (gen-ops md-files))

  (def html-dir "test/fixture/spbt/html")
  (def md-dir "test/fixture/spbt/md")
  ops
  (spit (:path op) (:text op))
  (fs/delete-if-exists (:path op)))