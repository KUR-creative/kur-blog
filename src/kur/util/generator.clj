(ns kur.util.generator
  (:require [clojure.spec.gen.alpha :as sg]
            [com.gfredericks.test.chuck.generators :as g']))

(defn string-from-regexes [re & res]
  (sg/fmap #(->> % (apply str) vec shuffle (apply str))
           (apply sg/tuple (map g'/string-from-regex (cons re res)))))

(comment
  (sg/sample (string-from-regexes #"asd"))
  )