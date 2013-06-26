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


(defn echo-handler [pickler]
  (proxy [HttpHandler] []
    (handle [exchange]
      (let [headers (pickler (.getRequestHeaders exchange))]
        (.add (.getResponseHeaders exchange)
              "Content-Type" "application/edn")
        (respond exchange (prn-str headers))))))