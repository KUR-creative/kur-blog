(ns kur.util.compare)

(defn =by [f x & ys]
  (if ys
    (apply = (map f (cons x ys)))
    true))