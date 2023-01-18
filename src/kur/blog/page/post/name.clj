(ns kur.blog.page.post.name
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sg]
            [clojure.string :as str]
            [kur.util.generator :refer [string-from-regexes]]
            [kur.util.regex :refer [hangul* alphanumeric*]]
            [kur.util.string :refer [digit?]]
            [kur.util.time :refer [time-format]]
            [medley.core :refer [assoc-some]]))

;;; Post id parts
(s/def ::author
  (s/and string? #(seq %) #(not (str/includes? % "."))
         #(not (digit? (last %)))))

(def create-time-fmt "YYMMddHHmm")
(def create-time-len (count create-time-fmt)) ; NOTE: it can be different! (eg. "YYY" -> 2022)
(s/def ::create-time ; md file creation time. 
  ; NOTE: It doesn't check date time validity (eg. 9999999999 is valid)
  (s/with-gen (s/and string? #(re-matches #"\d+" %) #(= (count %) 10))
    #(sg/fmap (fn [inst] (time-format create-time-fmt inst))
              (s/gen inst?))))

;;; Post file name parts
(defn id-info [post-id]
  (let [author-len (- (count post-id) create-time-len)
        [author create-time] ;; TODO: refactor using subs
        (map #(apply str %) (split-at author-len post-id))]
    {:author (when (s/valid? ::author author) author)
     :create-time (when (s/valid? ::create-time create-time) create-time)}))
(s/def ::id
  (s/with-gen (s/and string? #(every? some? (vals (id-info %))))
    #(sg/fmap (fn [[author create-time]] (str author create-time))
              (sg/tuple (s/gen ::author) (s/gen ::create-time)))))

(s/def ::meta-str ; + means public, else private.
  #{"+" "-"})

(def obsidian-title-symbol* #"[\!\,\ \.\+\=\-\_\(\)]*")
(def gen-post-title
  "<id>[.<meta>].<title>.md  NOTE: title can be empty string"
  (string-from-regexes obsidian-title-symbol* alphanumeric* hangul*))
(s/def ::title
  (s/with-gen (s/and string?
                     #(not (s/valid? ::meta-str
                                     (first (str/split % #"\." 2)))))
    (fn [] gen-post-title)))

;;; Post file name <-> parts round trip
(s/def ::file-name-parts
  (s/keys :req-un [::id] :opt-un [::meta-str ::title]))

(def post-extension "md")

(defn parts->fname
  "post-fname is (fs/file-name path). post-fname includes .extension."
  [fname-parts]
  (str (->> fname-parts
            ((juxt :id :meta-str :title))
            (remove nil?)
            (str/join ".")) "." post-extension))

(defn fname->parts
  "post-fname is (fs/file-name path). post-fname includes .extension."
  [post-fname]
  (let [base-name (fs/strip-ext post-fname)
        [id meta title] (str/split base-name #"\." 3)]
    (cond-> {}
      (s/valid? ::id id) (assoc :id id)
      (s/valid? ::meta-str meta) (assoc-some :meta-str meta
                                             :title title)
      (s/valid? ::title meta) (assoc :title (if title
                                              (str meta "." title)
                                              meta)))))

(defn valid? [fname-or-parts]
  (if (string? fname-or-parts)
    (and (= (fs/extension fname-or-parts) post-extension)
         (s/valid? ::file-name-parts (fname->parts fname-or-parts)))
    (s/valid? ::file-name-parts fname-or-parts)))

(comment
  (id-info "asd1234567890")
  (s/exercise ::author 20)
  (last (s/exercise ::create-time 50))
  (s/exercise ::id 20)
  (s/exercise ::meta-str)

  (require '[com.gfredericks.test.chuck.generators :as g'])
  (sg/sample (g'/string-from-regex obsidian-title-symbol*) 30)
  (sg/sample gen-post-title 30)
  (s/exercise ::title)
  (s/explain ::title "+.asdf")
  (s/explain ::title "-.asdf")
  (s/explain ::title ".asdf")

  (s/exercise ::file-name-parts 20)
  (sg/sample (s/gen ::file-name-parts) 20)

  ;; roundtrip test
  (require '[clojure.test.check.properties
             :refer [for-all] :rename {for-all defp}]
           '[clojure.test.check.clojure-test :refer [defspec]])
  (defspec fname-parts-roundtrip-test 1000
    (defp [parts (s/gen ::file-name-parts)]
      (= parts (fname->parts (parts->fname parts)))))
  (fname-parts-roundtrip-test)

  (assert (= (-> {:title "o뼐n춑튬꽑2쬞덈", :id "ng700"}
                 parts->fname fname->parts valid?)
             false))
  (fname->parts "asdw1234567890.zxc.a") ;; TODO: Is it problem?
  )