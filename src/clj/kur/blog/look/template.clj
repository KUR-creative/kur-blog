(ns kur.blog.look.template
  (:require [hiccup.page :refer [include-css include-js]]))

(def scale1-viewport
  [:meta {:name "viewport"
          :content "width=device-width, initial-scale=1"}])
(def charset-utf8 [:meta {:charset "utf-8"}])

(defn head [& {:keys [js-paths css-paths]}]
  [:head scale1-viewport charset-utf8
   (apply include-css css-paths)
   (apply include-js js-paths)])