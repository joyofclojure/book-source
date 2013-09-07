(defproject joy/music "1.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1835"]
                 [core.async "0.1.0-SNAPSHOT"]]
  :source-paths ["src/clj"]
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src/cljs"]
     :compiler
     {:output-to "dev-target/all.js"
      :optimizations :whitespace
      :pretty-print true}}
    {:source-paths ["src/cljs"]
     :compiler
     {:output-to "prod-target/all.js"
      :optimizations :advanced
      :externs ["externs.js"]
      :pretty-print false}}]})
