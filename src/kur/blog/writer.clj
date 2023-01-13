(ns kur.blog.writer ;; TODO: Updater(delete also?)
  (:require [babashka.fs :as fs]
            [kur.blog.look.post :as look-post]
            [kur.blog.page.post :as post]
            [kur.blog.page.post.diff :as post-diff]
            [kur.blog.look.tags :as look-tags]
            [kur.blog.page.tags :as tags]
            [kur.blog.look.home :as look-home]
            [kur.util.file-system :as uf]))

(defn post-set [md-dir]
  (->> md-dir uf/path-seq (map post/post) set))

(defn site
  "Return ([html-path page-html] ...). posts shuold be vector."
  [posts html-dir]
  (letfn [(html-path [fname] (str (fs/path html-dir fname)))]
    (map vector
         (conj (mapv #(html-path (post/html-file-name %)) posts)
               (html-path "tags.html")
               (html-path "home.html"))
         (conj (mapv #(look-post/html nil (:text %)) posts)
               (look-tags/html (tags/tag:posts posts)
                               (filter #(not (tags/has-tags? %)) posts))
               (look-home/html (sort-by :id ; id means creation time
                                        posts))))))

(defn write! [site]
  (run! (fn [[path html]] (spit path html)) site))

;;
(comment
  (def md-dir "test/fixture/blog-root/blog-md/")
  (def html-dir "test/fixture/blog-root/tmp-html/")

  (def a-site
    (site (map #(-> % post/post post/load-text) (uf/path-seq md-dir))
          html-dir))
  (write! a-site)

  (def old-state
    #{{:id "kur2207281052",
       :meta-str "+",
       :md-path "test/fixture/blog-root/blog-md/kur2207281052.+.md",
       :last-modified-millis 1673419774387}
      {:id "kur2207161305",
       :meta-str "+",
       :title "kill-current-sexp의 Emacs, VSCode 구현",
       :md-path "test/fixture/blog-root/blog-md/kur2207161305.+.kill-current-sexp의 Emacs, VSCode 구현.md",
       :last-modified-millis 1673419762417}
      {:id "kur0000000000",
       :md-path "test/fixture/blog-root/blog-md/kur0000000000.md",
       :last-modified-millis 1673419774387}
      {:id "kur2206082055",
       :title "Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다",
       :md-path "test/fixture/blog-root/blog-md/kur2206082055.Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다.md",
       :last-modified-millis 1673419825027}
      {:id "kur2301092038",
       :meta-str "+",
       :title "초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자",
       :md-path "test/fixture/blog-root/blog-md/kur2301092038.+.초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자.md",
       :last-modified-millis 1673419781187}
      {:id "kur2207111708",
       :title "Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다",
       :md-path
       "test/fixture/blog-root/blog-md/kur2207111708.Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다.md",
       :last-modified-millis 1673419745900}})
  (def new-state
    #{{:id "kur2207281052",
       :meta-str "+",
       :md-path "test/fixture/blog-root/blog-md/kur2207281052.+.md",
       :last-modified-millis 1673419774387}
      {:id "kur2207161305",
       :meta-str "+",
       :title "kill-current-sexp의 Emacs, VSCode 구현",
       :md-path "test/fixture/blog-root/blog-md/kur2207161305.+.kill-current-sexp의 Emacs, VSCode 구현.md",
       :last-modified-millis 1673419762417}
      {:id "kur2207111708",
       :meta-str "+",
       :title "Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다",
       :md-path
       "test/fixture/blog-root/blog-md/kur2207111708.Secret Manager 서비스는 어플리케이션의 secret을 안전하게 관리하여.. haha. 하드코딩된 secret을 없애고 주기적으로 자동화된 secret 변경이 가능케 한다.md",
       :last-modified-millis 1673579652440}
      {:id "kur2206082055",
       :title "Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다",
       :md-path "test/fixture/blog-root/blog-md/kur2206082055.Clojure 1.10의 tap은 디버깅 용도(better prn)로 사용할 수 있다.md",
       :last-modified-millis 1673419825027}
      {:id "kur2205182112",
       :title ".인간의 우열(편차)보다 사용하는 도구와 환경, 문화의 우열이 퍼포먼스에 더 큰 영향을 미친다",
       :md-path "test/fixture/blog-root/blog-md/kur2205182112..인간의 우열(편차)보다 사용하는 도구와 환경, 문화의 우열이 퍼포먼스에 더 큰 영향을 미친다.md",
       :last-modified-millis 1673391112645}
      {:id "kur2004250001",
       :meta-str "+",
       :title "오버 띵킹의 함정을 조심하라",
       :md-path "test/fixture/blog-root/blog-md/kur2004250001.-.오버 띵킹의 함정을 조심하라.md",
       :last-modified-millis 1673391124082}
      {:id "kur2301092038",
       :meta-str "+",
       :title "초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자",
       :md-path "test/fixture/blog-root/blog-md/kur2301092038.+.초등학교 덧셈 알고리즘을 함슬라믹하게 짜보자.md",
       :last-modified-millis 1673419781187}})

  (post-diff/happened-assocds #{} old-state)
  (def hap-assocds (vec (post-diff/happened-assocds old-state new-state)))

  (def cmds (map post/how-update-html hap-assocds)))