(ns kur.blog.obsidian.tag
  (:require
   [clojure.test.check.generators :as g]
   [clojure.spec.alpha :as s]
   [kur.util.generator :refer [string-from-regexes]]
   [kur.util.regex :refer [ascii* hangul*]]))

(def tag-character-regex
  "See https://help.obsidian.md/How+to/Working+with+tags#Allowed+characters"
  #"[a-z|A-Z|0-9|ㄱ-ㅎ|ㅏ-ㅣ|가-힣|\-_/]+")
(defn only-allowed-chars? [s]
  (re-matches tag-character-regex s))

(s/def ::tag
  (s/with-gen (s/and string? only-allowed-chars? #(nil? (parse-long %)))
    (fn [] (g/such-that only-allowed-chars?
                        (string-from-regexes ascii* hangul*)
                        200))))

(comment
  (g/sample (s/gen ::tag))
  (s/valid? ::tag "012"))