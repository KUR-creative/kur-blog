(ns kur.util.file-system
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]))

;;;
(s/def ::file-name ;; not a root
  (s/and #(not (#{"" "." ".."} (str %)))
         #(not (.contains (str %) "/"))
         #(< (count (.getBytes %)) 256) ;; linux limit
         ))

(s/def ::path (s/and string? #(not= (str %) "")))
;; NOTE: Valid unix path are way too robust.
;; See https://unix.stackexchange.com/questions/125522/path-syntax-rules
;; Maybe . .. / /// ~ etc.. are need to be supported. But not now!

(s/def ::extension
  (s/and #(not (.contains (str %) "/")) #(not= (first (str %)) \.)))

(s/def ::existing-path (s/and ::path fs/exists?))

;;;
(defn delete-all-except-gitkeep [dir]
  (->> (fs/list-dir dir)
       (remove #(= (fs/file-name %) ".gitkeep"))
       (run! fs/delete)))

(defn last-modified-millis [path]
  (-> path fs/last-modified-time fs/file-time->millis))

;;;
(defn path-seq
  "List all the file paths in root, recursively. 
   pred to filter the results, f to map the filtereds. 
   default pred: isFile, f: str"
  ;TODO: Use transducer instead? (perf)
  ([root] (path-seq root #(.isFile %) str))
  ([root pred] (path-seq root pred str))
  ([root pred f] (->> (file-seq (io/as-file root))
                      (filter pred) (map f))))

(defn paths-seq
  "List all the file paths in root*s* recursively, then cat the results.
   pred to filter the results, f to map the filtereds. 
   default pred: isFile, f: str"
  ;TODO: Use transducer instead? (perf)
  ([roots] (paths-seq roots #(.isFile %) str))
  ([roots pred] (paths-seq roots pred str))
  ([roots pred f] (->> (mapcat file-seq (map io/as-file roots))
                       (filter pred) (map f))))

(comment
  (path-seq "resource/res-roots/")
  (path-seq "resource/res-roots/" #(.isDirectory %))
  (path-seq "resource/res-roots/" #(.isFile %) identity)

  (paths-seq ["resource/res-roots/flat"
              "resource/res-roots/nested"]))