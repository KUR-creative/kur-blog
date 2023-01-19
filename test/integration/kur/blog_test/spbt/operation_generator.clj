(ns kur.blog-test.spbt.operation-generator
  (:require [clojure.test.check.generators :as g]))

(defn gen-create [md-files]
  (g/fmap #(assoc % :kind :create) (g/elements md-files)))
(defn gen-delete [md-files]
  (g/fmap #(hash-map :kind :delete :path (:path %))
          (g/elements md-files)))
#_(defn gen-update [md-files] ;; TODO: update file
    (g/fmap #(assoc % :kind :delete) (g/elements md-files)))
#_(defn gen-test [md-files] ;; test props
    (g/fmap #(assoc % :kind read) (g/elements md-files)))
(def gen-update-sys ;; NOTE: with timing에서는 제거해야..
  (g/return {:kind :upd-sys}))

(defn gen-ops [md-files]
  (def md-files md-files)
  (g/vector (g/one-of [(gen-create md-files)
                       (gen-delete md-files)
                       gen-update-sys])))

(comment
  (g/sample (gen-create md-files))
  (g/sample (gen-ops md-files)))