(ns joy.patterns.app)

(def config {:type :mock
             :lib  'joy.patterns.mock})

(defn initialize [cfg]
  (let [lib (:lib cfg)]
    lib))


(comment

  (initialize config)

)