(ns kur.blog.page.post.name.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(defn digit? [c]
  (and (>= 0 (compare \0 c)) (>= 0 (compare c \9))))

;;; Post id parts
(s/def ::author
  (s/and string? #(seq %) #(not (str/includes? % "."))
         #(not (digit? (last %)))))

(s/def ::create-time ; md file creation time. 
  ; NOTE: It doesn't check date time validity (eg. 9999999999 is valid)
  (s/and string? #(re-matches #"\d+" %) #(= (count %) 10)))

(def create-time-fmt "YYMMddHHmm")
(def create-time-len (count create-time-fmt)) ; NOTE: it can be different! (eg. "YYY" -> 2022)

;;; Post file name parts
(defn id-info [post-id]
  (let [author-len (- (count post-id) create-time-len)
        [author create-time] ;; TODO: refactor using subs
        (map #(apply str %) (split-at author-len post-id))]
    {:author (when (s/valid? ::author author) author)
     :create-time (when (s/valid? ::create-time create-time)
                    create-time)}))
(s/def ::id
  (s/and string? #(every? some? (vals (id-info %)))))

(s/def ::meta-str ; + means public, else private.
  #{"+" "-" nil})

(s/def ::title
  (s/nilable (s/and string?
                    #(not (s/valid? ::meta-str
                                    (first (str/split % #"\." 2)))))))

;; Post file name <-> parts round trip
(s/def ::file-name-parts
  (s/keys :req-un [::id] :opt-un [::meta-str ::title]))


(comment
  (s/exercise ::author 20)
  (last (s/exercise ::create-time 50))
  (s/exercise ::id 20)
  (s/exercise ::meta-str)

  (s/exercise ::title)
  (s/explain ::title "+.asdf")
  (s/explain ::title "-.asdf")
  (s/explain ::title ".asdf")

  (s/exercise ::file-name-parts 20)
  (sg/sample (s/gen ::file-name-parts) 20)

  (id-info "asd1234567890")
  (id-info "admin0000000000"))