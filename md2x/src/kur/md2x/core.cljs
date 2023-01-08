(ns kur.md2x.core)

(def ->obsidian
  (doto ((js/require "markdown-it")
         #js{:breaks true
             :html true
             :linkify true})
    (.use (js/require "markdown-it-mark"))))

(def exports
  #js {:obsidian (fn [md] ^js (.render ->obsidian md))})

(comment
  (require '["fs" :as fs])
  ((.-obsidian exports) "### test")
  ((.-obsidian exports) (fs/readFileSync "./README.md" "utf8"))
  )