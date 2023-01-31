(ns kur.util.string)

(defn digit? [c]
  (and (>= 0 (compare \0 c)) (>= 0 (compare c \9))))