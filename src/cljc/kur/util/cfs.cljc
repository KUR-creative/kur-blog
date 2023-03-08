(ns kur.util.cfs
  "Common or CLJS Compatible file system utilities"
  (:require [clojure.string :as str]))

(defn file-name [s]
  (re-find #"[^/]*$" s))

;; https://github.com/babashka/fs/blob/ceb8f7f48d4a1438a434ffc16238d5f1883bb4ac/src/babashka/fs.cljc#L743-L773
(defn split-ext
  "Splits path on extension If provided, a specific extension `ext`, the
  extension (without dot), will be used for splitting.  Directories
  are not processed."
  ([path] (split-ext path nil))
  ([path {:keys [ext]}]
   (let [path-str (str path)
         file-name (file-name path)]
     (let [ext (if ext
                 (str "." ext)
                 (when-let [last-dot (str/last-index-of file-name ".")]
                   (subs file-name last-dot)))]
       (if (and ext
                (str/ends-with? path-str ext)
                (not= path-str ext))
         (let [loc (str/last-index-of path-str ext)]
           [(subs path-str 0 loc)
            (subs path-str (inc loc))])
         [path-str nil])))))

(defn strip-ext
  "Strips extension via `split-ext`."
  ([path]
   (strip-ext path nil))
  ([path {:keys [ext] :as opts}]
   (first (split-ext path opts))))

(defn extension
  "Returns the extension of a file via `split-ext`."
  [path]
  (-> path split-ext last))

(comment
  [(file-name "asd/sdws/ad sws")
   (file-name "../sdws/ad sws")
   (file-name "~/ad sw.s")
   (file-name "./a/d sw-s")
   (file-name "a/d sws.a.wsd.ee")])