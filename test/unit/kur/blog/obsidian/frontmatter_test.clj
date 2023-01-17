(ns kur.blog.obsidian.frontmatter-test
  "Markdown frontmatter parser test

   An md doc shape:
         <------------ cap
   ---   <------------ head
   tags: [aa, bb]  <-- yaml
   ---   <------------ foot
   txt.. <------------ shoe"
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as g]
   [clojure.test.check.properties
    :refer [for-all] :rename {for-all defp}]
   [com.gfredericks.test.chuck.generators :as g']
   [kur.blog.obsidian.frontmatter :as frontmatter]
   [kur.util.regex :refer [common-whitespace*]]
   [clj-yaml.core :as yaml]))

;; cap
(def gen-ws-cap (g'/string-from-regex common-whitespace*))
(def gen-not-ws-cap (g/such-that #(not (.contains % "---\n"))
                                 (g/not-empty g/string-ascii)))
;; head
(def head "---\n")

;; yaml
(def gen-yaml (g/fmap yaml/generate-string g/any-printable-equatable))
(def gen-non-yaml (g/such-that #(and (not (frontmatter/parse-yaml %))
                                     (not (.contains % "---"))
                                     (not (.endsWith % "--"))
                                     (not (.endsWith % "-")))
                               g/string-ascii
                               100))
(def gen-no-tags-yaml
  (g/fmap #(yaml/generate-string (if (map? %) (dissoc % :tags) %))
          g/any-printable-equatable))
(defn gen-tags-yaml [tags]
  (g/fmap #(yaml/generate-string (if (map? %)
                                   (assoc % :tags tags)
                                   {:tags tags :xs %}))
          g/any-printable-equatable))

;; foot
(def foot "\n---")

;; shoe
;(def gen-shoe g/string-ascii)
(def gen-shoe
  (g'/string-from-regex #"([ㄱ-ㅎ|ㅏ-ㅣ|가-힣]|[ -~]|[-\n])*"))
(def gen-no-foot-shoe
  (g/such-that #(not (.contains % "---")) gen-shoe))

(defn gen-frontmatter-str [gen-yaml]
  (g/let [cap (g/one-of [gen-ws-cap gen-not-ws-cap])
          head (s/gen #{head ""})
          yaml gen-yaml
          foot (s/gen #{foot ""})]
    (str cap head yaml foot)))

;; Tests
(defn nil-all-case
  "All args are generators. yaml=nil, body=all case"
  [cap head yaml foot shoe]
  (g/let [c cap, h head, y yaml, f foot, s shoe]
    (let [inp (str c h y f s)]
      {:input inp, :frontmatter nil, :body inp})))

(defspec cap-is-not-ws-then-yaml-is-nil 100
  (defp [m (nil-all-case gen-not-ws-cap (s/gen #{head ""})
                         (g/one-of [gen-yaml gen-non-yaml])
                         (s/gen #{foot ""}) gen-shoe)]
    (= (frontmatter/parse (:input m)) (dissoc m :input))))

(defspec head-is-not-exists-then-yaml-is-nil 100
  (defp [m (nil-all-case gen-not-ws-cap (g/return "")
                         (g/one-of [gen-yaml gen-non-yaml])
                         (s/gen #{foot ""}) gen-shoe)]
    (= (frontmatter/parse (:input m)) (dissoc m :input))))

(defspec no-foot-then-yaml-is-nil 100
  (defp [m (nil-all-case gen-ws-cap (s/gen #{head ""})
                         (g/one-of [gen-yaml gen-non-yaml])
                         (g/return "") gen-no-foot-shoe)]
    (= (frontmatter/parse (:input m)) (dissoc m :input))))

(def head-foot-exists
  (g/let [cap gen-ws-cap
          y (g/one-of [gen-yaml gen-non-yaml])
          s gen-shoe]
    (let [inp (str cap "---\n" y "\n---" s)]
      {:parts [cap "---\n" y "\n---" s]
       :input inp, :frontmatter (frontmatter/parse-yaml y), :body s})))
(defspec head-foot-exists-case 1000
  (defp [m head-foot-exists]
    (= (frontmatter/parse (:input m)) (select-keys m [:frontmatter :body]))))

;;
(comment
  (frontmatter/parse "---\n@----") (println "---\n@----")
  (frontmatter/parse "---\n0\n---asdf")
  (frontmatter/parse "---\n12345 234\n---asdf")
  (frontmatter/parse "---\n- {'---': 0}\n---") (println "---\n- {'---': 0}\n---")
  (frontmatter/parse "---\ntags: [---]\n---")
  (frontmatter/parse "---\ntags: [가나다, 라마]\n---테트\n테스트")
  (frontmatter/parse "---\n---aaa")
  (frontmatter/parse "---\n---")
  (frontmatter/parse "---\n0\n\n---")

  (do
    (cap-is-not-ws-then-yaml-is-nil)
    (head-is-not-exists-then-yaml-is-nil)
    (no-foot-then-yaml-is-nil)
    (head-foot-exists-case)))