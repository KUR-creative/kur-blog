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
    :upd-sys
    (let [new-posts (updater/post-set md-dir)

          {:keys [unchangeds to-deletes to-writes]}
          (updater/classify-posts old-posts new-posts)

          loaded-to-writes (map post/load-text to-writes)

          site (updater/site unchangeds to-deletes loaded-to-writes
                             html-dir)]
      (updater/update! site)
      (into (set unchangeds) loaded-to-writes))))