(ns kur.blog.state
  "Currently, the state is set of post"
  (:require [kur.blog.page.post :as post]
            [kur.blog.updater :as updater]))

(defn initial
  "Create initial state of application"
  []
  #{})

(defn state
  "Current state calculated from markdown directory"
  [md-dir]
  (updater/post-set md-dir))

(defn current
  "Current state and site"
  [old-state {:keys [md-dir html-dir] :as config}]
  (let [new-state (state md-dir)

        {:keys [to-deletes to-writes unchangeds]}
        (updater/classify-posts old-state new-state)

        loaded-to-writes (map post/load-text to-writes)]
    {:state (into (set unchangeds) loaded-to-writes)
     :site (updater/site unchangeds to-deletes loaded-to-writes
                         html-dir)}))

(defn update!
  [atom-old-state current]
  ;(def current-s current)
  (updater/update! (:site current))
  (reset! atom-old-state (:state current)))

(comment
  (count (:site current-s))
  (map #(take 2 %) (:site current-s)))