(ns kur.blog.obsidian.frontmatter
  "Obsidian markdown frontmatter parser
   
   An md doc shape:
         <------------ cap
   ---   <------------ head
   tags: [aa, bb]  <-- yaml
   ---   <------------ foot
   txt.. <------------ shoe"
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]))

(defn parse-yaml
  "Return yaml parsed data. if error occurs, return nil"
  [s]
  (try
    (yaml/parse-string s)
    (catch Exception _
      nil)))

(defn parse [md]
  (let [[trimmed-cap rest] (str/split (str/triml md) #"---\n" 2)
        [yaml-str shoe] (when rest (str/split rest #"\n---" 2))]
    (if (or (not= trimmed-cap "") (nil? rest) (nil? shoe))
      {:frontmatter nil :body md}
      {:frontmatter (parse-yaml yaml-str) :body shoe})))