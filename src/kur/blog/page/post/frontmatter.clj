(ns kur.blog.page.post.frontmatter
  "Markdown frontmatter parser
   "
  (:require [clj-yaml.core :as yaml]))

(defn yaml-parse
  "Return yaml parsed data. if error occurs, return nil"
  [s]
  (try
    (yaml/parse-string s)
    (catch Exception _
      nil)))

(defn obsidian [md]
  (let [[_ frontmatter-str body]
        (re-find #"(?s)^[ \t\n\r]*---(\n.*\n|\n)---(.*)" md)]
    (if frontmatter-str
      {:frontmatter (yaml-parse frontmatter-str) :body body}
      {:frontmatter nil :body md})))