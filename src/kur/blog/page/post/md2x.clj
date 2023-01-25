(ns kur.blog.page.post.md2x
  "Convert markdown to someting(x)"
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io])
  (:import (com.eclipsesource.v8 NodeJS)))

(def md2x-path "./md2x/out/md2x.js")

;;
(def ^:private inp-chan (async/chan))
(def ^:private out-chan (async/chan))
(def ^:private md2x-loop
  ;TODO? Any problem when program exits? Need expilict (shutdown-agents)?
  (async/thread
    (let [md2x (.require (NodeJS/createNodeJS) (io/file md2x-path))
          md->obsidian-html
          #(.executeJSFunction md2x "obsidian" (to-array [%]))]
      (while true
        (->> (async/<!! inp-chan)
             md->obsidian-html (async/>!! out-chan))))))

(defn obsidian-html [md]
  (async/>!! inp-chan md) (async/<!! out-chan))

;;
(comment
  (obsidian-html "### 3")
  (obsidian-html (slurp "./README.md"))
  (spit "out/t.html" (obsidian-html (slurp "./README.md"))))