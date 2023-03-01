(ns kur.blog.look.template
  (:require [hiccup.element :refer [link-to]]
            [hiccup.page :refer [include-css include-js]]
            [kur.blog.policy :as policy]))

(def scale1-viewport
  [:meta {:name "viewport"
          :content "width=device-width, initial-scale=1"}])

(def charset-utf8 [:meta {:charset "utf-8"}])

(def google-analytics-script
  (slurp "src/html/google-analytics.html"))

(def favicon
  [:link {:href policy/favicon :rel "icon" :type "image/x-icon"}])

(defn head [& {:keys [js-paths css-paths title]}]
  [:head
   scale1-viewport
   charset-utf8
   google-analytics-script
   favicon
   (apply include-css css-paths)
   (apply include-js js-paths)
   [:title title]])

;; Common
(defn heading-id [heading]
  (-> heading clojure.string/trim (clojure.string/replace #"\s+" "-")))

(defn only-cursor-link-to [url content]
  (link-to {:class "only-cursor"} url content))

(defn heading-link [heading]
  (only-cursor-link-to (str "#" (heading-id heading)) heading))

(def navigation
  [:nav {:class "navigation"}
   (link-to {:class "only-cursor"} "archive" "archive")
   (link-to {:class "only-cursor"} "tags" "tags")
   (link-to {:class "only-cursor"} "subscribe" "subscribe")
   (link-to {:class "only-cursor"} "guests" "guests")
   [:img {:src policy/search-logo :height "20px" :width "20px"}]])
   ;; TODO: link to google site search url

(def header
  [:header {:class "container"}
   [:hr]
   [:div
    {:class "site-title"}
    (link-to {:class "only-cursor"} "/" "KUR Creative")]
   [:hr]
   navigation
   [:hr]])

(defn article-page
  [{:keys [css-paths title]} {:keys [h1 content]}]
  (list (head :css-paths (if css-paths
                           css-paths
                           policy/common-css-paths)
              :title title)
        [:body
         header
         [:article {:class "container"}
          (when (seq h1) [:h1 h1])
          (when content content)]]))