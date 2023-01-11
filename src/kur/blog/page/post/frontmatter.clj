(ns kur.blog.page.post.frontmatter
  "Markdown frontmatter parser
   
   An md doc shape:
         <------------ cap
   ---   <------------ head
   tags: [aa, bb]  <-- frontmatter
   ---   <------------ foot
   txt.. <------------ shoe"
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]))

(defn yaml-parse
  "Return yaml parsed data. if error occurs, return nil"
  [s]
  (try
    (yaml/parse-string s)
    (catch Exception _
      nil)))

(defn obsidian [md]
  (let [[trimmed-cap rest] (str/split (str/triml md) #"---\n" 2)
        [frontmatter-str shoe] (when rest (str/split rest #"\n---" 2))]
    (if (or (not= trimmed-cap "") (nil? rest) (nil? shoe))
      {:frontmatter nil :body md}
      {:frontmatter (yaml-parse frontmatter-str) :body shoe})))