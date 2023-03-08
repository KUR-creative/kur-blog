(ns kur.md2x.wikilink.token
  (:require [kur.md2x.config :refer [config]]))

(defn resource-path [path] ;NOTE: policy
  (str (:resource-dir config) "/" path))

(defn resource-extension? [ext] ;NOTE: policy
  ;; TODO: need config. can check file existence from md-dir of config
  (and (string? ext)
       (re-find #"^[0-9a-z]+$" ext)))

(defn Token
  ([state type tag nesting]
   (new (.-Token ^js state) type tag nesting))
  ([state type tag nesting js-obj]
   (.assign js/Object
            (new (.-Token ^js state) type tag nesting)
            js-obj)))

(defn text [state token digested-info]
  #js[(Token state "text" "" 0
             #js{:content (:s digested-info)})])

(defn link [state token {:keys [path text extension]}]
  (let [level (.-level token)
        href (if (resource-extension? extension)
               (resource-path path)
               path)]
    ;(prn extension)
    [(Token state "link_open" "a" 1
            #js{:attrs #js[#js["href" href]]
                :level level :markup "wikilink" :info "auto"})
     (Token state "text" "" 0
            #js{:content (if text text path)
                :level (inc level)})
     (Token state "link_close" "a" -1
            #js{:level level :markup "wikilink" :info "auto"})]))

(defmulti embed
  (fn [_ _ digested-info]
    (:extension digested-info)))

(defn img [state token {:keys [path text]}]
  (let [res-path (resource-path path)
        some-text (if text text res-path)] ; some means 'not nil'
    [(Token state "image" "img" 0
            #js{:attrs #js[#js["src" res-path]
                           #js["alt" some-text]]
                :content some-text
                :children #js[(Token state "text" "" 0
                                     #js{:content some-text})]
                :markup "wikilink"})]))
(defmethod embed "png" [state token digested-info]
  (img state token digested-info))
(defmethod embed "jpg" [state token digested-info]
  (img state token digested-info))
(defmethod embed "jpeg" [state token digested-info]
  (img state token digested-info))

(defn video [state token {:keys [path text]}]
  (let [level (.-level token)
        res-path (resource-path path)]
    [(Token state "video_open" "video" 1
            #js{:attrs #js[#js["src" res-path]
                           #js["autoplay" ""]; turns autoplay=""
                           #js["muted" ""]
                           #js["loop" ""]]
                :level level :markup "wikilink" :info "auto"})
     (Token state "text" "" 0
            #js{:content (if text text path)
                :level (inc level)})
     (Token state "video_close" "video" -1
            #js{:level level :markup "wikilink" :info "auto"})]))
(defmethod embed "mp4" [state token digested-info]
  (video state token digested-info))

(defmethod embed :default [state token digested-info]
  (text state token digested-info))