(ns joy.web
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [com.sun.net.httpserver HttpHandler
            HttpExchange HttpServer]
           [java.net InetSocketAddress HttpURLConnection]
           [java.io IOException FilterOutputStream
            BufferedInputStream FileInputStream]
           [java.net URLDecoder]))
    
(defn echo-handler [pickler]
  (proxy [HttpHandler] []
    (handle [exchange]
      (let [headers (pickler (.getRequestHeaders exchange))]
        (.add (.getResponseHeaders exchange)
              "Content-Type" "application/edn")
        (respond exchange (prn-str headers))))))


(defn new-server
  [port path handler]
  (doto (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))


