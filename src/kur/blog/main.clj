(ns kur.blog.main
  (:require [cprop.core :refer [load-config]]))

(comment
  (def cfg (load-config :file "test/fixture/config/small.edn")))