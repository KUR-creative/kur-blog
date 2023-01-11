(ns kur.blog.page.post
  (:require [babashka.fs :as fs]
            [kur.blog.page.post.frontmatter :as frontmatter]
            [kur.blog.page.post.name :refer [fname->parts]]
            [kur.util.file-system :as uf]))

(defn post [path]
  (-> path fs/file-name str fname->parts
      (assoc :last-modified-millis (uf/last-modified-millis path)
             :md-path path)))
; :last-modified-millis를 체크해서 새 post가 기존과 같으면 기존 post를 반환,
; 디스크 io를 줄일 수는 있다

(defn content [post]
  (frontmatter/obsidian (-> post :md-path slurp)))

;; policies
(defn public? [post]
  (= "+" (:meta-str post)))

(defn html-file-name [post]
  (str (:id post) ".html"))

;;
(comment
  (def troot "test/fixture/blog-root/blog-md/")
  (post (str troot "kur2004250001.-.오버 띵킹의 함정을 조심하라.md"))
  (post (str troot "kur2206082055.Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다.md"))
  (post (str troot "kur2207281052.+.md")))