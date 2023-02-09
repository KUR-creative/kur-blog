(ns kur.md2x.core
  (:require [clojure.string :as s]))

(defn token? [token type]
  (and (.hasOwnProperty token "type")
       (= (.-type token) type)))

(defn has-wikilinks? [content]
  (re-find #"\[\[.+\]\]" content))

(defn wikilink-inline? [token]
  {:pre [(some? token)]}
  (and (token? token "inline")
       (has-wikilinks? (.-content token))))

(defn wiki-token? [token]
  {:pre [(some? token)]}
  (and (token? token "text")
       (has-wikilinks? (.-content token))))

(defn Token
  ([state type tag nesting]
   (new (.-Token ^js state) type tag nesting))
  ([state type tag nesting js-obj]
   (.assign js/Object
            (new (.-Token state) type tag nesting)
            js-obj)))

(defn wikilink-subs-ranges
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
         ;; Check embeding -> ![[*]]
         (map #(let [beg (:beg %), beg-1 (dec beg)
                     embed? (and (<= 0 beg-1) (= (get s beg-1) \!))]
                 (assoc %
                        :beg (if embed? beg-1 beg)
                        :embed? embed?))))))

(defn cut-wikilink-content
  "Cut token.content str into normal text parts and wiki link parts.

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
        ranges (wikilink-subs-ranges content)

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
  (def info info)
  (let [[path text] (s/split (subs s (if embed? 3 2) (- (count s) 2)) ; 3=![[ 2=[[ or ]]
                             #"\|" 2)
        [path text] [(s/trim path) (when text (s/trim text))] ;NOTE: obsidian feature
        [_ & rest-path] (s/split path #"\.")
        extension (when rest-path (last rest-path))]
    (prn (cond-> info
           wikilink? (assoc :path path)
           text (assoc :text text)
           extension (assoc :extension extension)))
    (cond-> info
      wikilink? (assoc :path path)
      text (assoc :text text) ; text=nil: no | in link. ex) [[no-pipe]]
      extension (assoc :extension extension))))

(defmulti parsed-tokens
  (fn [_ _ digested-info]
    (def digested-info digested-info)
    (select-keys digested-info [:wikilink?])))

(defmethod parsed-tokens {:wikilink? false}
  [state token digested-info]
  #js[(Token state "text" "" 0 #js{:content (:s digested-info)})])

(defmethod parsed-tokens {:wikilink? true}
  [state token digested-info]
  [(Token state "link_open" "a" 1 ;; TODO: remove #js
          #js{:attrs #js[#js["href" "TODO-URL"]]
              :level (.-level token)
              :markup "wikilink"
              :info "auto"})
   (Token state "text" "" 0
          #js{:content (:s digested-info)
              :level (inc (.-level token))})
   (Token state "link_open" "a" 1
          #js{:level (.-level token)
              :markup "wikilink"
              :info "auto"})])

;(parsed-tokens (first parts))
#_(js/console.log (new (.-Token state)))
(defn parse-wiki-token
  "Return wikilink parsing result(token seq) of 'text' token"
  [state token]
  {:pre [(some? token)]}
  (def token token)
  (let [parts (cut-wikilink-content (.-content token))
        digesteds (map digest-wikilink-info parts)
        xs (mapcat #(parsed-tokens state token %) digesteds)]
    (def parts parts)
    (def xs xs)
    (def digesteds digesteds)
    xs))
;(js/console.log (nth xs 0))
#_(js/console.log (clj->js xs))

(defn change
  "Token change: split children by wiki-link rule."
  [state token]
  {:pre [(some? token)]}
  {:children (mapcat #(if (wiki-token? %)
                        (parse-wiki-token state %)
                        [%])
                     (.-children token))})

(defn wikilink-rule [state]
  (def state state) (def ts (.-tokens state)) (def inlines (map #(when (= (.-type %) "inline") %) (.-tokens state)))
  (let [tokens (.-tokens state)
        _ (def tokens tokens)
        changes (->> tokens
                     (map #(when (wikilink-inline? %) %))
                     (map #(when % (change state %)))
                     clj->js)]
    (def changes changes)
    (run! (fn [[token change]]
            ;(js/console.log token) (prn change) (prn "----")
            (when change
              (js/Object.assign token change)))
          (map vector tokens changes))))

(do
  (def mdit ((js/require "markdown-it")
             #js{:linkify true}))
  (-> ^js (.-core mdit)
      (.-ruler)
      (.after "linkify" "mine" wikilink-rule)))

(comment
  ^js (.render mdit "[[]]")
  ^js (.render mdit "png]]")
  ^js (.render mdit "[[png]]")
  ^js (.render mdit "[[abc.png]]")
  ^js (.render mdit "![[abc.png|aaasss]]")
  ^js (.render mdit "![[abc|aaas|ss]]")
  ^js (.render mdit "![[abc.p.ng|aaas|ss]]")
  ^js (.render mdit "![[abc.p.s.xng|aaas|s|s]]")
  ^js (.render mdit "![[abc.png]]")
  (js/console.log "---------------------")
  (js/console.log ts)
  (js/console.log (get (.-tokens state) 1))
  (js/console.log (get (.-tokens state) 6))
  ^js (.render mdit "aa [[abc.png  ]] bb")
  ^js (.render mdit "[[aa ]]![[abc.png]] bb")
  ^js (.render mdit "- [[abc.png]] [t](a)\n  - x [[bb.asd]] y")
  ^js (.render mdit "http://test.com")
  ^js (.render mdit "- http://test.com")
  ^js (.render mdit "- http://test.com\n  - http://a.b")
  ^js (.render mdit "abc http://a.b def http://a.b xx")
  ^js (.render mdit "abc http://a.b def \n - aa \n\n http://a.b xx")
; TODO: Add inline html too

  (js/console.log state)
  (js/console.log tokens)
  (js/console.log (clj->js inlines))

  (js/console.log (get (.-tokens state) 3))
  (js/console.log (get (.-tokens state) 8))

  (run! #(js/console.log %)
        (keep #(when (= (.-type %) "inline") %)
              (.-tokens state)))

  (js/console.log ^js (.-tokens state)))
(def s "012[[567[[012]]567]]0![[456]]90")
(def s "01a3[[678]]1234")
(def s "[[234]]")
(def content s)
(def pairs (wikilink-subs-ranges s))
(map (fn [[beg end]] (subs s beg end)) pairs)

(def ws ["012[[567[[012]]567]]0![[456]]90"
         "[[234]]"   "[[234]]78"  "01[[456]]"  "01[[456]]90"
         "![[345]]" "![[345]]89" "01![[567]]" "01![[567]]01"
         "[[23]][[89]]![[56]]"
         "not a wlink"])
(try
  (def results (map #(hash-map :w %1 :pairs %2 :c %3)
                    ws
                    (map wikilink-subs-ranges ws)
                    (map cut-wikilink-content ws)))
  (def old-results results)
  (run! println
        (map (fn [{w :w pairs :pairs c :c}]
               (str w
                    #_"\t->\t" #_(map (fn [[beg end]]
                                        (if beg (subs w beg end) w))
                                      pairs)
                    "\t->\t" c))
             results))
  (catch :default e
    (js/console.log e)))

(println "same? :" (= old-results results))

(def exports
  #js{:obsidian (fn [md] ^js (.render mdit md))})