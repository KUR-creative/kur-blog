(ns kur.blog.page.post.frontmatter-test
  "Markdown frontmatter parser test

  the md doc shape:
        <------------ cap
  ---   <------------ head
  tags: [aa, bb]  <-- frontmatter
  ---   <------------ foot
  txt.. <------------ shoe"
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as g]
   [clojure.test.check.properties
    :refer [for-all] :rename {for-all defp}]
   [com.gfredericks.test.chuck.generators :as g']
   [kur.blog.page.post.frontmatter :refer :all]
   [kur.util.regex :refer [common-whitespace*]]
   [clj-yaml.core :as yaml]))

;; cap
(def gen-ws-cap (g'/string-from-regex common-whitespace*))
(def gen-not-ws-cap (g/such-that #(not (.contains % "---\n"))
                                 (g/not-empty g/string-ascii)))
;; head
(def head "---\n")

;; frontmatter
(def gen-yaml-io (g/fmap yaml/generate-string g/any-printable-equatable))
(def gen-non-yaml (g/such-that #(and (not (yaml-parse %))
                                     (not (.contains % "---"))
                                     (not (.endsWith % "--"))
                                     (not (.endsWith % "-")))
                               g/string-ascii
                               100))

;; foot
(def foot "\n---")

;; shoe
(def gen-shoe g/string-ascii)

;; Tests
(defn nil-all-case
  "All args are generators. frontmatter=nil, body=all case"
  [cap head frontmatter foot shoe]
  (g/let [c cap, h head, fm frontmatter, f foot, s shoe]
    (let [inp (str c h fm f s)]
      {:input inp, :frontmatter nil, :body inp})))

(defspec cap-is-not-ws-then-fm-is-nil 100
  (defp [m (nil-all-case gen-not-ws-cap (s/gen #{head ""})
                         (g/one-of [gen-yaml-io gen-non-yaml])
                         (s/gen #{foot ""}) gen-shoe)]
    (= (obsidian (:input m)) (dissoc m :input))))

(defspec head-is-not-exists-then-fm-is-nil 100
  (defp [m (nil-all-case gen-not-ws-cap (g/return "")
                         (g/one-of [gen-yaml-io gen-non-yaml])
                         (s/gen #{foot ""}) gen-shoe)]
    (= (obsidian (:input m)) (dissoc m :input))))

(defspec no-foot-then-no-fm 100
  (defp [m (nil-all-case gen-ws-cap (g/return head)
                         (g/one-of [gen-yaml-io gen-non-yaml])
                         (g/return "") gen-shoe)]
    (= (obsidian (:input m)) (dissoc m :input))))

(def head-foot-exists
  (g/let [cap gen-ws-cap
          fm (g/one-of [gen-yaml-io gen-non-yaml])
          s gen-shoe]
    (let [inp (str cap "---\n" fm "\n---" s)]
      {:parts [cap "---\n" fm "\n---" s]
       :input inp, :frontmatter (yaml-parse fm), :body s})))
(defspec head-foot-exists-case 1000
  (defp [m head-foot-exists]
    (= (obsidian (:input m)) (select-keys m [:frontmatter :body]))))

;;
(comment
  (obsidian "---\n@----") (println "---\n@----")
  (obsidian "---\n0\n---asdf")
  (obsidian "---\n12345 234\n---asdf")
  (obsidian "---\n- {'---': 0}\n---") (println "---\n- {'---': 0}\n---")
  (obsidian "---\ntags: [---]\n---")
  (obsidian "---\ntags: [가나다, 라마]\n---테트\n테스트")
  (obsidian "---\n---aaa")
  (obsidian "---\n---")
  (obsidian "---\n0\n\n---")

  (do
    (cap-is-not-ws-then-fm-is-nil)
    (head-is-not-exists-then-fm-is-nil)
    (no-foot-then-no-fm)
    (head-foot-exists-case)))