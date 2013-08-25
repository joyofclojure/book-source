(ns joy.externs-for-cljs
  (:require [clojure.java.io :as io]
            [cljs.compiler :as comp]
            [cljs.analyzer :as ana]
            [clojure.pprint :refer [pprint]]))

(def code-string "(defn hello [x] (js/alert (pr-str 'greetings x)))")
(def code-data (read-string code-string))
(def ast (ana/analyze (ana/empty-env) code-data))

(defn print-ast [ast]
  (pprint  ;; pprint indents output nicely
    (clojure.walk/prewalk ;; rewrite each node of the ast
      (fn [x]
        (if (map? x)
          (select-keys x [:children :name :form :op]) ;; return selected entries of each map node
          x))  ;; non-map nodes are left unchanged
      ast)))


(defn file-ast [file]
  (binding [ana/*cljs-ns* 'cljs.user
            ana/*cljs-file* file]
    (mapv #(ana/analyze (ana/empty-env) %)
          (read-string (str "[" (slurp file) "]")))))

(defn flatten-ast [ast]
  (mapcat #(tree-seq :children :children %) ast))

(defn get-vars-used [flat-ast]
  (->> flat-ast
       (filter #(and (= (:op %) :var) (-> % :info :ns)))
       (map #(-> % :info :name))
       distinct))

(defn var-defined? [sym]
  (contains? (:defs (get @ana/namespaces (symbol (namespace sym))))
             (symbol (name sym))))

(defn get-undefined-vars [ffa]
  (remove var-defined? (get-vars-used ffa)))

(defn externs-for-var [sym]
  (if (= "js" (namespace sym))
    (format "var %s={};\n" (name sym))
    (format "var %s={};\n%s.%s=function(){};\n"
            (namespace sym) (namespace sym) (name sym))))

(defn get-interop-used [ffa]
  (->> ffa
       (filter #(and (= (:op %) :dot)))
       (map #(or (:method %) (:field %)))
       distinct))

(defn externs-for-interop [sym]
  (format "DummyExternClass.%s=function(){};\n" sym))

(defn externs-for-cljs [file]
  (swap! ana/namespaces empty)
  (ana/analyze-file "cljs/core.cljs")
  (let [ffa (flatten-ast (file-ast file))]
    (apply str
           (concat
            (map externs-for-var (get-undefined-vars ffa))
            ["var DummyExternClass={};\n"]
            (map externs-for-interop (get-interop-used ffa))))))

;; (externs-for-cljs "/home/chouser/proj/cljs-example/src/main.cljs")
