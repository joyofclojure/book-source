(ns joy.web
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]))

(def OK java.net.HttpURLConnection/HTTP_OK)


(defn respond
  ([exchange body]
    (respond identity exchange body))
  ([around exchange body]
    (.sendResponseHeaders exchange OK 0)
    (with-open [resp (around (.getResponseBody exchange))]
      (.write resp (.getBytes body)))))


(defn default-handler [txt]
  (proxy [HttpHandler] []
    (handle [exchange]
      (respond exchange txt))))


(defn new-server
  [port path handler]
  (doto (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))


(comment
  (def server (new-server 8123 "/joy/hello" (default-handler "Hello Cleveland")))
  (.stop server 0)

  (def p (default-handler "There's no problem that can't be solved with another level of indirection."))
  (def server (new-server 8123 "/" p))
)


(comment

  (update-proxy p {"handle" (fn [this exchange] (respond exchange (str "this is " this)))})

)

(def echo-handler
  (fn [_ exchange]
    (let [headers (.getRequestHeaders exchange)]
      (respond exchange (prn-str headers)))))

(comment

  (update-proxy p {"handle" echo-handler})

  '{"Cache-control" ("max-age=0"), "Host" ("localhost:8123"), "Accept-charset" ("ISO-8859-1,utf-8;q=0.7,*;q=0.3"), "Accept-encoding" ("gzip,deflate,sdch"), "Connection" ("keep-alive"), "Accept-language" ("en-US,en;q=0.8"), "User-agent" ("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.63 Safari/537.31"), "Accept" ("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")}

)

(defn listing [file]
  (-> file .list sort))

(comment

  (listing (io/file "."))

  ;;=> (".gitignore" "README.md" "project.clj" "src" "target" "test")

)

(defn html-links [root things]
  (string/join
   (for [file things]
     (str "<a href='"
          (str root (if (= "/" root) "" File/separator) file)
          "'>"
          file "</a><br>"))))

(comment

  (html-links "." (listing (io/file ".")))

  ;;=> "<a href='./.gitignore'>.gitignore</a><br><a href='./README.md'>README.md</a><br>
  ;; <a href='./project.clj'>project.clj</a><br><a href='./src'>src</a><br>
  ;; <a href='./target'>target</a><br><a href='./test'>test</a><br>"
)

(defn details [file]
  (str (.getName file) " is "
       (.length file)  " bytes."))

(comment

  (details (io/file "./README.md"))

  ;;=> "README.md is 330 bytes."

)

(defn uri->file [root uri]
  (->> uri
       str
       URLDecoder/decode
       (str root)
       io/file))

(comment
  
  (uri->file "." (URI. "/project.clj"))

  ;;=> #<File ./project.clj>

  (details (uri->file "." (URI. "/project.clj")))

  ;;=> "project.clj is 289 bytes."
)

(defn html-around [o]
  (proxy [FilterOutputStream] [o]
    (write [raw-bytes]
      (proxy-super write
        (.getBytes (str "<html><body>"
                        (String. raw-bytes)
                        "</body></html>"))))))

(def fs-handler
  (fn [_ exchange]
    (let [uri  (.getRequestURI exchange)
          file (uri->file "." uri)]
      (if (.isDirectory file)
        (do (.add (.getResponseHeaders exchange)
                  "Content-Type" "text/html")
            (respond html-around
                     exchange
                     (html-links (str uri) (listing file))))
        (respond exchange (details file))))))

(comment

  (update-proxy p {"handle" fs-handler})

)
