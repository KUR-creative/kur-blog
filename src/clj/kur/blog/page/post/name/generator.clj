(ns kur.blog.page.post.name.generator
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [kur.blog.page.post.name.spec :as spec]
            [kur.util.generator :refer [string-from-regexes]]
            [kur.util.regex :refer [alphanumeric* hangul*]]
            [kur.util.time :refer [time-format]]))

(def create-time
  (sg/fmap (fn [inst] (time-format spec/create-time-fmt inst))
           (s/gen inst?)))

(def id
  (sg/fmap (fn [[author create-time]] (str author create-time))
           (sg/tuple (s/gen ::spec/author)
                     (s/gen ::spec/create-time
                            {::spec/create-time (fn [] create-time)}))))

(def obsidian-title-symbol* #"[\!\,\ \.\+\=\-\_\(\)]*")
(def post-title
  "<id>[.<meta>].<title>.md  NOTE: title can be empty string"
  (string-from-regexes obsidian-title-symbol* alphanumeric* hangul*))

(comment
  (require '[com.gfredericks.test.chuck.generators :as g'])
  (sg/sample (g'/string-from-regex obsidian-title-symbol*) 30)
  (sg/sample post-title 30))