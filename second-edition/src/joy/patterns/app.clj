(ns joy.patterns.app
  (require [joy.patterns.di :as di]))

(def config {:type :mock
             :lib  'joy.patterns.mock})

(defn initialize [cfg]
  (let [lib (:lib cfg)]
    (require lib)
    (di/)))


(comment

  (initialize config)

)