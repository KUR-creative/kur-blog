(ns kur.md2x.main
  (:require [kur.md2x.wikilink.plugin :refer [enable-wikilink!]]))

(def mdit
  (doto ((js/require "markdown-it")
         #js{:linkify true
             :html true
             :breaks true})
    (.use (js/require "markdown-it-mark"))
    (.use enable-wikilink!)))

(def exports
  #js{:obsidian (fn [md] ^js (.render mdit md))})

(comment
  (do
    (def cases
      [;; Invalid
       "[[]]"         "<p>[[]]</p>\n"
       "png]]"        "<p>png]]</p>\n"
     ;; Normal wikilink
       "[[png]]"              "<p><a href=\"png\">png<a></p>\n"
       "[[abc.png]]"          "<p><a href=\"abc.png\">abc.png<a></p>\n"
       "[[abc.png|aaasss]]"   "<p><a href=\"abc.png\">aaasss<a></p>\n"
     ;; Embedded wikilink with correct/incorrect/no extension
       "![[abc.png|aaasss]]"        "<p><img src=\"resource/abc.png\" alt=\"aaasss\"></p>\n"
       "![[abc|aaas|ss]]"           "<p>![[abc|aaas|ss]]</p>\n"
       "![[abc.p.ng|aaas|ss]]"      "<p>![[abc.p.ng|aaas|ss]]</p>\n"
       "![[abc.p.s.xng|aaas|s|s]]"  "<p>![[abc.p.s.xng|aaas|s|s]]</p>\n"
     ;; Embedding img
       "![[abc.png]]"
       "<p><img src=\"resource/abc.png\" alt=\"resource/abc.png\"></p>\n"
       "![[abc.jpg]]"
       "<p><img src=\"resource/abc.jpg\" alt=\"resource/abc.jpg\"></p>\n"
       "![[abc.jpeg]]"
       "<p><img src=\"resource/abc.jpeg\" alt=\"resource/abc.jpeg\"></p>\n"
     ;; Trimming white space
       "aa [[abc.png  ]] bb"
       "<p>aa <a href=\"abc.png\">abc.png<a> bb</p>\n"
       "[[aa ]]![[abc.png]] bb"
       "<p><a href=\"aa\">aa<a><img src=\"resource/abc.png\" alt=\"resource/abc.png\"> bb</p>\n"
     ;; Nested list
       "- [[abc.png]] [t](a)\n  - x [[bb.asd]] y"
       "<ul>\n<li><a href=\"abc.png\">abc.png<a> <a href=\"a\">t</a>\n<ul>\n<li>x <a href=\"bb.asd\">bb.asd<a> y</li>\n</ul>\n</li>\n</ul>\n"
     ;; mark
       "==test==" "<p><mark>test</mark></p>\n"
     ;; linkify
       "http://test.com"
       "<p><a href=\"http://test.com\">http://test.com</a></p>\n"])

    (def md->html (.-obsidian exports))
    (keep (fn [[md html]]
            (let [out (md->html md)]
              (when-not (= html out)
                {:md md
                 :expected html
                 :actual out})))
          (partition 2 cases))))