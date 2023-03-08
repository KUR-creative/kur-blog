(ns kur.md2x.wikilink.token
  (:require [kur.md2x.config :refer [config]]
            [kur.blog.policy :as policy]
            [kur.blog.page.post.name :as name]))

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

(defn render-link-info
  "Render url and text of anchor from digested info. NOTE: policy"
  [{:keys [path text heading extension]}]
  (let [{:keys [id title] :as parts} (name/basename->parts path)
        valid-path? (name/valid-parts parts)
        norm-title (when title (policy/normalize-title title))
        url (cond-> (if valid-path? (str id "." norm-title) path)
              (seq heading) (str "#" (policy/slugify heading))
              (resource-extension? extension) (resource-path))]
    {:url url :txt (if (seq text)
                     text
                     (if valid-path? norm-title url))}))

(defn link [state token digested-info]
  (let [level (.-level token)
        {:keys [url txt]} (render-link-info digested-info)]
    [(Token state "link_open" "a" 1
            #js{:attrs #js[#js["href" url]]
                :level level :markup "wikilink" :info "auto"})
     (Token state "text" "" 0 #js{:content txt :level (inc level)})
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

(defmethod embed "png"  [state token info] (img state token info))
(defmethod embed "jpg"  [state token info] (img state token info))
(defmethod embed "jpeg" [state token info] (img state token info))
(defmethod embed "gif"  [state token info] (img state token info))
(defmethod embed "avif" [state token info] (img state token info))
(defmethod embed "png"  [state token info] (img state token info))
(defmethod embed "svg"  [state token info] (img state token info))
(defmethod embed "svgz" [state token info] (img state token info))
(defmethod embed "tif"  [state token info] (img state token info))
(defmethod embed "tiff" [state token info] (img state token info))
(defmethod embed "wbmp" [state token info] (img state token info))
(defmethod embed "webp" [state token info] (img state token info))
(defmethod embed "ico"  [state token info] (img state token info))
(defmethod embed "jng"  [state token info] (img state token info))
(defmethod embed "bmp"  [state token info] (img state token info))

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