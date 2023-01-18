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
(def gen-tags-and-frontmatter
  (g/let [tags (g/one-of [g/any-printable-equatable
                          (g/vector (s/gen ::tag/tag))])
          frontmatter (fmt/gen-frontmatter-str
                       (g/one-of [fmt/gen-non-yaml
                                  fmt/gen-no-tags-yaml
                                  (fmt/gen-tags-yaml tags)]))]
    {:tags tags :frontmatter frontmatter}))

(defn gen-md-file [dir]
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
  [op [old-posts new-posts html-dir]]
  (case (:kind op)
    :create  (try (spit (:path op) (:text op))
                  (catch Exception e (prn 'c) (def op op)))
    :delete  (try (fs/delete-if-exists (:path op))
                  (catch Exception e (prn 'd) (def op op)))
    :upd-sys (updater/update! (updater/site old-posts new-posts html-dir))))

;; Properties
(defn same-num-public-pages? [model html-dir]
  (def mcnt (count model)) ; 2 = tags, home
  (def m model)
  (def x (uf/path-seq html-dir
                      #(and (= (fs/extension %) "html")
                            (not= (fs/file-name %) "tags.html")
                            (not= (fs/file-name %) "home.html"))))
  (def acnt (count (uf/path-seq html-dir
                                #(and (= (fs/extension %) "html")
                                      (not= (fs/file-name %) "tags.html")
                                      (not= (fs/file-name %) "home.html")))))
  (= (+ (count model))
     (count (uf/path-seq html-dir
                         #(and (= (fs/extension %) "html")
                               (not= (fs/file-name %) "tags.html")
                               (not= (fs/file-name %) "home.html"))))))

;; Test
;(def cnt (atom 0))
(defspec test-without-timing
  {:num-tests 100
   ;:seed 1674007981491
   ;:max-size 60
   }
  (let [md-dir "test/fixture/spbt/md"
        html-dir "test/fixture/spbt/html"]
    (defp [ops #_(g/bind (g/set (gen-md-file md-dir) {:min-elements 1})
                         gen-ops)
           #_(g/return [{:path "test/fixture/spbt/md/A7001010900.+.md", :text "", :kind :create}
                        {:kind :upd-sys}])
           (g/return
            [{:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}
             {:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}])]
      (def ops ops)
      (uf/delete-all-except-gitkeep md-dir)
      (uf/delete-all-except-gitkeep html-dir)

      ;(reset! cnt 0) (println "") ;;;
      (loop [posts (updater/post-set md-dir), model {}, ops ops]
        (if-let [op (first ops)]
          (let [new-posts (updater/post-set md-dir)]
            (next-actual op [posts new-posts html-dir])
            (if (or (not= (:kind op) :upd-sys)
                    (same-num-public-pages? model html-dir)) ; prop 1
              (recur new-posts (next-model op model) (rest ops))
              false))
          true)) ;; pass test 
      )))

(def m0 {})
(def m1 (next-model {:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}
                    m0))
(next-actual {:path "test/fixture/spbt/md/A7001010900.+.md", :text "", :kind :create}
             nil)

(def m2 (next-model {:path "test/fixture/spbt/md/A7001010900.+..md", :text "", :kind :create}
                    m1))

(comment
  (g/sample gen-md-text)
  (g/sample gen-invalid-post-fname)
  (g/sample gen-valid-post-fname)
  (g/sample gen-post-name)
  (g/sample gen-tags-and-frontmatter)
  (g/sample (g/set (gen-md-file "test/fixture/spbt/md/")))
  (run! (fn [{:keys [path text]}] (spit path text))
        (-> "test/fixture/spbt/md/"
            gen-md-file g/set g/sample last))

  (def md-files (last (g/sample (g/set (gen-md-file "test/fixture/spbt/md/")))))
  (g/sample (gen-create md-files))
  (g/sample (gen-ops md-files))

  (def html-dir "test/fixture/spbt/html")
  (def md-dir "test/fixture/spbt/md")
  ops
  (test-without-timing)
  (spit (:path op) (:text op))
  (fs/delete-if-exists (:path op)))