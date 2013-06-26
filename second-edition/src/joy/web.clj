(ns joy.web
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [com.sun.net.httpserver HttpHandler
            HttpExchange HttpServer]
           [java.net InetSocketAddress HttpURLConnection]
           [java.io IOException FilterOutputStream
            BufferedInputStream FileInputStream]
           [java.net URLDecoder]))
    
(defn respond [exchange body]
  (.sendResponseHeaders exchange HttpURLConnection/HTTP_OK 0)
  (doto (.getResponseBody exchange)
    (.write (.getBytes body))
    (.close)))


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


(def echo-handler
  (fn [_ exchange]
    (let [headers (merge {} (.getRequestHeaders exchange))]
      (println headers)
      (respond exchange (prn-str headers)))))

(comment

  (update-proxy p {"handle" (fn [this exchange] (respond exchange "foo"))})
  
  (update-proxy p {"handle" echo-handler})

)

(defn listing [file]
  (-> file .list sort))

(defn html
  [root things]
  (apply str
         (concat
          ["<html><head></head><body>"]
          (for [f things]
            (str "<a href='"
                 (str root (if (= "/" root) "" File/separator) f)
                 "'>"
                 f "</a><br>"))
          ["</body></html>"])))

(def fs-handler
  (fn [_ exchange]
    (let [uri (URLDecoder/decode (str (.getRequestURI exchange)))
          f (File. (str "." uri))
          filenames (listing f)]
      (if (.isDirectory f)
        (do (.add (.getResponseHeaders exchange)
                  "Content-Type" "text/html")
            (respond exchange (html uri filenames)))
        (respond exchange "A file")))))

(comment

  (update-proxy p {"handle" fs-handler})

)
