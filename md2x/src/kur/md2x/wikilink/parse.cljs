(ns kur.md2x.wikilink.parse
  (:require [clojure.string :as s]))

(defn subs-ranges
  "Return [info*] from s (the content of token). 
   start is start index, end is exclusive index as (subs start end).

   1) scan and get [[*]]. example:
   s = 01a3[[678]]123456
         ~~           ~~
         ^            ^
       s[i]  = a      last-beg = 15
        s01  = a3
   then beg = 4, end = 11. (subs beg end) = [[678]]
   
   2) check embedding: ![[  "
  [s]
  (let [last-beg (- (count s) 2) ;; 2 = size of sliding window
        s01 (fn [i] (subs s i (+ i 2)))]
    (->> (loop [i 0, beg nil, ranges []] ;; Scanning
           (cond (> i last-beg)   ranges
                 (= (s01 i) "[[") (recur (inc i) i ranges)
                 (and beg (= (s01 i) "]]"))
                 (recur (inc i) nil (conj ranges {:beg beg
                                                  :end (+ i 2)
                                                  :wikilink? true}))
                 :else            (recur (inc i) beg ranges)))
         ;; Check embeding: ![[*]]
         (map #(let [beg (:beg %), beg-1 (dec beg)
                     embed? (and (<= 0 beg-1) (= (get s beg-1) \!))]
                 (assoc %
                        :beg (if embed? beg-1 beg)
                        :embed? embed?))))))

(defn cut-content
  "Cut token.content str into normal text parts and wiki link parts.
  Return seq of wikilink information map  

  (example)
  content   =    012[[567[[012]]567]]0![[456]]90123456
                         ^      ^     ^       ^       ^
  ranges    =          ([8     15]  [21      29])     |
  headed    = [[nil 0]  [8     15]  [21      29]]     |
  tailed    = ([nil 0]  [8     15]  [21      29]    [37 nil]) 
  cut-ranges=       [0 8] [8 15] [15 21] [21 29] [29 37]
                0        8      15     21       29      37 
  (:s info)s= ( 012[[567 [[012]] 567]]0 ![[456]] 90123456 )"
  [content]
  (let [len (count content)
        ranges (subs-ranges content)

        head {:end (if (= (:beg (first ranges)) 0) nil 0)}
        headed (vec (cons head ranges))

        tail {:beg (if (= (-> headed peek :beg) len) nil len)}
        tailed (conj headed tail)]
    (->> (mapcat (fn [{a :beg b :end :as ab} {x :beg}] ; merge
                   (if (= b x)
                     [ab]
                     [ab {:beg b :end x :wikilink? false}]))
                 tailed (rest tailed))
         (filter #(and (:beg %) (:end %))) ; cut-ranges
         (map #(-> %
                   (assoc :s (subs content (:beg %) (:end %)))
                   (dissoc :beg :end))))))

(defn digest-wikilink-info
  "Return digested wikilink information map"
  [{:keys [s wikilink? embed?] :as info}]
  (let [[path+heading text]
        (s/split (subs s (if embed? 3 2) (- (count s) 2)) #"\|" 2) ; 3=![[ 2=[[ or ]]

        [path+heading text]
        [(s/trim path+heading) (when text (s/trim text))] ;NOTE: obsidian feature

        [path heading] (s/split path+heading #"#" 2)
        [_ & rest-path] (s/split path #"\.")
        extension (when rest-path (last rest-path))]
    (cond-> info
      wikilink? (assoc :path path)
      heading (assoc :heading heading)
      text (assoc :text text) ; text=nil: no "|" in link. ex) [[no-pipe]]
      extension (assoc :extension extension))))

(comment
  (do
    (def ws ["012[[567[[012]]567]]0![[456]]90"
             "[[234]]"   "[[234]]78"  "01[[456]]"  "01[[456]]90"
             "![[345]]" "![[345]]89" "01![[567]]" "01![[567]]01"
             "[[23]][[89]]![[56]]"
             "not a wlink"])
    (def results (map #(hash-map :w %1 :pairs %2 :c %3)
                      ws
                      (map subs-ranges ws)
                      (map cut-content ws)))
    ;(def old-results results)

    (run! println
          (map (fn [{w :w pairs :pairs c :c}]
                 (str w
                      #_"\t->\t" #_(map (fn [[beg end]]
                                          (if beg (subs w beg end) w))
                                        pairs)
                      "\t->\t" c))
               results))))