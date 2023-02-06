(ns kur.md2x.core)

(do
  (def mdit ((js/require "markdown-it")
             #js{:linkify true}))
  (-> ^js (.-core mdit)
      (.-ruler)
      (.after
       "linkify" "mine"
       (fn [state]
         (def state state)
         (def ts (.-tokens state))
         (def inlines (map #(when (= (.-type %) "inline") %)
                           (.-tokens state)))
         (prn state)))))

^js (.render mdit "[[abc.png]]")

(defn Token
  ([state type tag nesting]
   (new (.-Token state) type tag nesting))
  ([state type tag nesting js-obj]
   (js/Object.assign (new (.-Token state) type tag nesting) js-obj)))

(defn wiki-tokens [state token]
  #js[(Token state "link_open" "a" 1
             #js{:attrs #js[#js["href" "TODO-URL"]]
                 :level (inc (.-level token))
                 :markup "wikilink"
                 :info "auto"})
      (Token state "text" "" 0
             #js{:content "TODO-text"
                 :level (.-level token)})
      (Token state "link_open" "a" 1
             #js{:level (dec (.-level token))
                 :markup "wikilink"
                 :info "auto"})])
(js/console.log
 (wiki-tokens state (->> inlines second (.-children) js->clj first)))

^js (.render mdit "- [[abc.png]] [t](a)\n  - x [[bb.asd]] y")
^js (.render mdit "- http://test.com")
^js (.render mdit "- http://test.com\n  - http://a.b")

(comment
  (js/console.log state)

  (js/console.log ts)
  (js/console.log (get (.-tokens state) 1))
  (js/console.log (get (.-tokens state) 3))
  (js/console.log (get (.-tokens state) 8))

  (run! #(js/console.log %)
        (keep #(when (= (.-type %) "inline") %)
              (.-tokens state)))

  (js/console.log inlines)
  (js/console.log ^js (.-tokens state)))


(def exports
  #js{:obsidian (fn [md] ^js (.render mdit md))})