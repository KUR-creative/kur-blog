(ns kur.md2x.wikilink.token
  (:require [kur.md2x.config :refer [config]]))

(defn resource-path [path]
  (str (:resource-dir config) "/" path))

(defn Token
  ([state type tag nesting]
   (new (.-Token ^js state) type tag nesting))
  ([state type tag nesting js-obj]
   (.assign js/Object
            (new (.-Token state) type tag nesting)
            js-obj)))

(defn just-text-tokens [state token digested-info]
  #js[(Token state "text" "" 0
             #js{:content (:s digested-info)})])

(defn link-tokens [state token {:keys [path text]}]
  (let [level (.-level token)]
    [(Token state "link_open" "a" 1
            #js{:attrs #js[#js["href" path]]
                :level level :markup "wikilink" :info "auto"})
     (Token state "text" "" 0
            #js{:content (if text text path)
                :level (inc level)})
     (Token state "link_open" "a" 1
            #js{:level level :markup "wikilink" :info "auto"})]))

(defmulti embed-tokens
  (fn [_ _ digested-info]
    (:extension digested-info)))

(defn img-tokens [state token {:keys [path text]}]
  (let [res-path (resource-path path)
        some-text (if text text res-path)] ; some means 'not nil'
    [(Token state "image" "img" 0
            #js{:attrs #js[#js["src" res-path]
                           #js["alt" some-text]]
                :content some-text
                :children #js[(Token state "text" "" 0
                                     #js{:content some-text})]
                :markup "wikilink"})]))

(defmethod embed-tokens "png" [state token digested-info]
  (img-tokens state token digested-info))
(defmethod embed-tokens "jpg" [state token digested-info]
  (img-tokens state token digested-info))
(defmethod embed-tokens "jpeg" [state token digested-info]
  (img-tokens state token digested-info))
(defmethod embed-tokens :default [state token digested-info]
  (just-text-tokens state token digested-info))