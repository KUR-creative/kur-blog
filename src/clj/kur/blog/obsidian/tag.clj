(ns kur.blog.obsidian.tag
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as g]
            [kur.util.generator :refer [string-from-regexes]]))

(def tag-character-regex
  "See https://help.obsidian.md/How+to/Working+with+tags#Allowed+characters"
  #"[a-z|A-Z|0-9|ㄱ-ㅎ|ㅏ-ㅣ|가-힣|\-_/]+")
(defn only-allowed-chars? [s]
  (re-matches tag-character-regex s))

(s/def ::tag
  (s/with-gen (s/and string? only-allowed-chars? #(nil? (parse-long %)))
    #(string-from-regexes tag-character-regex)))

(defn valid? [tag-str]
  (s/valid? ::tag tag-str))

(comment
  (g/sample (s/gen ::tag))
  (s/valid? ::tag "012")
  (s/valid? ::tag "a 012")
  (s/valid? ::tag "s/테스트용series후후/a1000"))