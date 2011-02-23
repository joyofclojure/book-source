(ns joy.web
  "The simple web service example from section 10.1"
  (:import (com.sun.net.httpserver HttpHandler HttpExchange HttpServer)
           (java.net InetSocketAddress HttpURLConnection)
           (java.io IOException FilterOutputStream)
           (java.util Arrays)))
    
(defn default-handler [txt] ;; #: Create default handler
  (proxy [HttpHandler] []
    (handle [exchange]
      (.sendResponseHeaders exchange HttpURLConnection/HTTP_OK 0)
      (doto (.getResponseBody exchange)
        (.write (.getBytes txt)) ;; #: Close over txt
        (.close)))))

(defn new-server [port path handler] ;; #: Create service
  (doto (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))

(comment    
  (def server (new-server 8123 "/joy/hello" (default-handler "Hello Cleveland")))

  (.stop server 0)
  (def p (default-handler "There's no problem that can't be solved with another level of indirection"))
  (def server (new-server 8123 "/joy/hello" p))
)

(defn make-handler-fn [fltr txt]
  (fn [this exchange] ;; #: Name explicit this
    (let [b (.getBytes txt)]
      (-> exchange
          .getResponseHeaders
          (.set "Content-Type" "text/html"))
      (.sendResponseHeaders exchange 
                            HttpURLConnection/HTTP_OK 
                            0)
      (doto (fltr (.getResponseBody exchange)) ;; #: Pass through filter
        (.write b)
        (.close)))))

(defn change-message
  "Convenience method to change a proxy's output message"
  ([p txt] (change-message p identity txt))                     ;; #: Use identity filter
  ([p fltr txt]
     (update-proxy p 
       {"handle" (make-handler-fn fltr txt)})))

(comment
  (change-message p "Hello Dynamic!")
)

(defn screaming-filter [o]
  (proxy [FilterOutputStream] [o]
    (write [b]
      (proxy-super write (.getBytes (str "<strong>" 
                                         (.toUpperCase (String. b)) 
                                         "</strong>"))))))

(comment
  (change-message p screaming-filter "whisper"))
