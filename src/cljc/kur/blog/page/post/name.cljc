(ns kur.blog.page.post.name
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [kur.blog.page.post.name.spec :as spec]
            [kur.util.cfs :as cfs]
            [medley.core :refer [assoc-some]]))

(def id-info spec/id-info)

;; Post file name <-> parts round trip
(def post-extension "md")
(defn parts->fname
  "post-fname is (cfs/file-name path). post-fname includes .extension."
  [fname-parts]
  (str (->> fname-parts
            ((juxt :id :meta-str :title))
            (remove nil?)
            (str/join ".")) "." post-extension))

(defn fname->parts
  "post-fname is (cfs/file-name path). post-fname includes .extension."
  [post-fname]
  (let [base-name (cfs/strip-ext post-fname)
        [id meta title] (str/split base-name #"\." 3)]
    (cond-> {:meta-str nil :title nil}
      (s/valid? ::spec/id id) (assoc :id id)
      (s/valid? ::spec/meta-str meta) (assoc-some :meta-str meta
                                                  :title title)
      (s/valid? ::spec/title meta) (assoc :title (if title
                                                   (str meta "." title)
                                                   meta)))))
;; path or parts(id, meta, title) validation
(defn file-path? [x] (and (string? x) (not= x "")))
(defn valid-parts
  "Return valid file name parts or nil if invalid"
  [path-or-parts]
  ;(def path-or-parts path-or-parts)
  (cond (file-path? path-or-parts) ; arg = path
        (let [fname (cfs/file-name path-or-parts)
              parts (fname->parts fname)]
          (if (and (= (cfs/extension fname) post-extension) ; policy
                   (not (.contains fname ".sync-conflict-"))
                   (s/valid? ::spec/file-name-parts parts))
            parts
            nil))

        (s/valid? ::spec/file-name-parts path-or-parts) ; arg = parts
        path-or-parts

        :else
        nil))

(comment
  (fname->parts "asdw1234567890.zxc.a") ;; TODO: Is it problem?
  (fname->parts "min0000000404.+.404 Not Found.md"))