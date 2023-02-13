(ns kur.md2x.main)

(def mdit ((js/require "markdown-it")
           #js{:linkify true}))
#_(-> ^js (.-core mdit)
      (.-ruler)
      (.after "linkify" "mine" wikilink-rule))

(def exports
  #js{:obsidian (fn [md] ^js (.render mdit md))})