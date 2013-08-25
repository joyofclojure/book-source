(defproject second-edition "0.1.0"
  :description "Example sources for the second edition of JoC"
  :url "http://www.joyfoclojure.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1835"]
                 [org.clojure/core.unify "0.5.3"]
                 [org.clojure/core.logic "0.8.0"]]
  :source-paths ["src/clj"]
  :aot [joy.gui.DynaFrame])
