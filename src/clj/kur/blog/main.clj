(ns kur.blog.main
  (:require [cprop.core :refer [load-config]]
            [kur.blog.monitor :as monitor]
            [kur.blog.state :as state]
            [kur.util.file-system :as uf])
  (:gen-class))

(defn -main [config-path]
  (let [{:keys [md-dir html-dir fs-wait-ms]
         :as config} (load-config :file config-path)
        state (atom (state/initial))
        update! #(state/update! state (state/current @state config))]
    (def state state) (map :id @state)

    ;; Initialize
    (uf/delete-all-except-gitkeep html-dir)
    (update!)

    ;; Monitor and Update
    (monitor/monitor update! fs-wait-ms md-dir)))

(comment
  #_(def cfg (load-config :file "test/fixture/config/small.edn"))
  #_(def cfg "test/fixture/config/default.edn")
  (do
    (monitor/stop! monitor)
    (def monitor (-main "test/fixture/config/default.edn"))))