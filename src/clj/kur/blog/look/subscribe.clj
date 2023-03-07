(ns kur.blog.look.subscribe
  (:require [hiccup.page :refer [html5]]
            [kur.blog.look.template :refer [article-page]]))

(defn html []
  (html5 (article-page
          {:title "KUR Creative Blog - Subscribe & Guestbook"}
          {:content
           (list [:h2 "Subscribe (공사중...)"]
                 "곧 이메일 구독 기능이 추가됩니다."
                 [:h2 "Guest Book (공사중...)"]
                 "곧 댓글 기능과 함께 방명록이 추가됩니다.")})))