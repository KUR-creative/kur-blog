(ns kur.blog-test.spbt.runner
  (:require [babashka.fs :as fs]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.name :as name]
            [kur.blog.updater :as updater]))

(defn next-model [{:keys [path text kind] :as op} model]
  (case kind
    :create  (let [parts (name/fname->parts (fs/file-name path))]
               (if (and (name/valid? (fs/file-name path))
                        (post/public? parts))
                 (assoc model path text)
                 model))
    :delete  (dissoc model path)
    :upd-sys model))

(defn next-actual
  "The actual state is a directory in file system!"
  [op [old-posts md-dir html-dir]]
  (case (:kind op)
    :create  (do (spit (:path op) (:text op)) old-posts)
    :delete  (do (fs/delete-if-exists (:path op)) old-posts)
    :upd-sys (let [new-posts (updater/post-set md-dir)]
               (def old-posts old-posts)
               (def new-posts new-posts)
               (def s (updater/site old-posts new-posts html-dir))
               (updater/update! (updater/site old-posts new-posts html-dir))
               new-posts)))
