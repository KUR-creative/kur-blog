(ns kur.blog.obsidian.embed
  "Embed image, video, youtube, ..."
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]))

;    text: (Don't support internal note embedding!)
;   image: https://help.obsidian.md/Linking+notes+and+files/Embedding+files#Embed+an+image+in+a+note
;   video: Just use <video> tag
; youtube: https://github.com/samwarnick/obsidian-simple-embeds

(defn img
  ([src] (img src src))
  ([src alt] (img src alt nil))
  ([src alt name:value]
   (html [:img (merge (update-keys name:value keyword)
                      {:src src :alt alt})])))

(comment
  (str/replace "하지만 나 같은 vim틀딱에게 kill-current-sex었다.
![[꼬우면아시죠.jpg]]
구현 자체는 코너 케이스만 잘 처리하면 된다.
![[kill-current-sexp.mp4]]
문제는 몇몇 코너 케이스인데, 커서가 붉은색에 위치할 때, 
(s (expre (ssion)) sexp) => (s (expre (ssion)) "
               #"!\[\[(.+)\]\]"
               (fn [[_ match]]
                 (img (str "/resource/" match))))

  (println (img "Asymm.png" "Ac/crphy.png" {"alt" "asdfed.png" "1" 2})))
