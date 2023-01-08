(ns kur.util.time
  (:import (java.time LocalDateTime ZoneId)
           (java.time.format DateTimeFormatter)))

(defn local-date-time
  ([inst] (local-date-time inst (ZoneId/systemDefault)))
  ([inst time-zone-id] (LocalDateTime/ofInstant (.toInstant inst)
                                                time-zone-id)))

(defn time-format [fmt inst]
  (.format (DateTimeFormatter/ofPattern fmt) (local-date-time inst)))

;;
(comment
  (local-date-time (java.util.Date.))
  (local-date-time (java.util.Date.) (ZoneId/systemDefault))

  (time-format "YYMMdd-HHmm" (java.util.Date.)))