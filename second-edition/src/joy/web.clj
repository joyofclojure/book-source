(ns joy.web
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress HttpURLConnection]
           [java.net URLDecoder URI]))

(defn respond
  ([exchange body]
    (respond identity exchange body))
  ([around exchange body]
    (.sendResponseHeaders exchange HttpURLConnection/HTTP_OK 0)
    (with-open [response (around (.getResponseBody exchange))]
      (.write response (.getBytes body)))))


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

  (def p (default-handler "There's no problem that can't be solved with another level of indirection"))
  (def server (new-server 8123 "/" p))
)


(comment

  (update-proxy p {"handle" (fn [this exchange] (respond exchange "foo"))})

)

(def echo-handler
  (fn [_ exchange]
    (let [headers (.getRequestHeaders exchange)]
      (respond exchange (prn-str headers)))))

(comment

  (update-proxy p {"handle" echo-handler})

)

(defn listing [file]
  (-> file .list sort))

(comment

  (listing (io/file "."))

  ;;=> (".gitignore" "README.md" "project.clj" "src" "target" "test")

)

(defn html [root things]
  (string/join
   (for [file things]
     (str "<a href='"
          (str root (if (= "/" root) "" File/separator) file)
          "'>"
          file "</a><br>"))))

(comment

  (html "." (listing (io/file ".")))

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

)

(def fs-handler
  (fn [_ exchange]
    (let [uri  (.getRequestURI exchange)
          file (uri->file "." uri)]
      (if (.isDirectory file)
        (do (.add (.getResponseHeaders exchange)
                  "Content-Type" "text/html")
            (respond exchange (html uri (listing file))))
        (respond exchange (details file))))))

(comment

  (update-proxy p {"handle" fs-handler})

)
