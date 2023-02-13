(ns kur.md2x.wikilink.plugin
  (:require [kur.md2x.wikilink.token :as token]
            [kur.md2x.wikilink.parse :as parse]))

;; predicates
(defn token? [token type]
  (and (.hasOwnProperty token "type")
       (= (.-type token) type)))

(defn has-wikilinks? [content]
  (re-find #"\[\[.+\]\]" content))

(defn wikilink-inline? [token]
  {:pre [(some? token)]}
  (and (token? token "inline")
       (has-wikilinks? (.-content token))))

(defn wiki-token? [token]
  {:pre [(some? token)]}
  (and (token? token "text")
       (has-wikilinks? (.-content token))))

;; parsing
(defn parsed-tokens
  [state token {:keys [wikilink? embed?] :as digested-info}]
  (cond
    (not wikilink?) (token/just-text-tokens state token digested-info)
    (not embed?) (token/link-tokens state token digested-info)
    :else (token/embed-tokens state token digested-info)))

(defn parse-wiki-token
  "Return wikilink parsing result(token seq) of 'text' token"
  [state token]
  {:pre [(some? token)]}
  (->> (parse/cut-wikilink-content (.-content token))
       (map parse/digest-wikilink-info)
       (mapcat #(parsed-tokens state token %))))

(defn change
  "Token change: split children by wiki-link rule."
  [state token]
  {:pre [(some? token)]}
  {:children (mapcat (fn [child]
                       (if (wiki-token? child)
                         (parse-wiki-token state child)
                         [child]))
                     (.-children token))})

;; rule and plugin
(defn wikilink-rule [state]
  ;(def state state) (def ts (.-tokens state)) (def inlines (map #(when (= (.-type %) "inline") %) (.-tokens state)))
  (let [tokens (.-tokens ^js state)
        changes (->> tokens
                     (map #(when (wikilink-inline? %) %))
                     (map #(when % (change state %)))
                     clj->js)]
    (run! (fn [[token change]]
            (when change
              (js/Object.assign token change)))
          (map vector tokens changes))))

(defn enable-wikilink!
  "An markdown-it plugin is procedure that changes MarkdownIt object."
  [mdit]
  (let [ruler (.-ruler (.-core ^js mdit))]
    (.after ruler "linkify" "mine" wikilink-rule)))

#_(do
    (def mdit ((js/require "markdown-it")
               #js{:linkify true}))
    (-> ^js (.-core mdit)
        (.-ruler)
        (.after "linkify" "mine" wikilink-rule)))