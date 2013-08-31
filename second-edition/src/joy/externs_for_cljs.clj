(ns joy.externs-for-cljs
  (:require [cljs.compiler :as comp]
            [cljs.analyzer :as ana]
            [clojure.walk :refer [prewalk]]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io])
  (:import (clojure.lang LineNumberingPushbackReader)))

(def code-string "(defn hello [x] (js/alert (pr-str 'greetings x)))")
(def code-data (read-string code-string))
(def ast (ana/analyze (ana/empty-env) code-data))

(defn print-ast [ast]
  (pprint  ;; pprint indents output nicely
    (prewalk ;; rewrite each node of the ast
      (fn [x]
        (if (map? x)
          (select-keys x [:children :name :form :op]) ;; return selected entries of each map node
          x))  ;; non-map nodes are left unchanged
      ast)))

;; (comp/emit ast)

(defn read-file
  "Read the contents of filename as a sequence of Clojure values."
  [filename]
  (let [eof (Object.)]
    (with-open [reader (LineNumberingPushbackReader. (io/reader filename))]
      (doall ;; force whole seq so it doesn't escape the with-open
       (take-while #(not= % eof)
                   (repeatedly #(read reader false eof)))))))

(defn file-ast
  "Return the ClojureScript AST for the contents of filename. Tends to
  be large and to contain cycles -- be careful printing at the REPL."
  [filename]
  (binding [ana/*cljs-ns* 'cljs.user ;; default namespace
            ana/*cljs-file* filename]
    (mapv #(ana/analyze (ana/empty-env) %)
          (read-file filename))))

(defn flatten-ast [ast]
  (mapcat #(tree-seq :children :children %) ast))

;; (def flat-ast (flatten-ast (file-ast "cljs/src/joy/music.cljs")))
;; (count flat-ast)

(defn get-interop-used
  "Return a set of symbols representing the method and field names
  used in interop forms in the given sequence of AST nodes."
  [flat-ast]
  (set (keep #(some % [:method :field]) flat-ast)))

(defn externs-for-interop [syms]
  (apply str
         "var DummyClass={};\n"
         (map #(str "DummyClass." % "=function(){};\n")
              syms)))

;; To distinguish ClojureScript vars from JavaScript class names that
;; must be extern'ed, we must find all vars referenced, and then
;; ignore those that we know to be defined as ClojureScript vars. This
;; requires paying attention to the compiler's idea of namespaces and
;; the vars defined in them, which is global mutable state.

(defn reset-namespaces!
  "Clears out the ClojureScript compilers mutable set of namespaces,
  replacing it with just cljs.core and the vars defined therein."
  []
  (swap! ana/namespaces empty)
  (ana/analyze-file "cljs/core.cljs"))

(defn var-defined?
  "Returns true if the given fully-qualified symbol is known by the
  ClojureScript compiler to have been defined, based on its mutable set
  of namespaces."
  [sym]
  (contains? (:defs (get @ana/namespaces (symbol (namespace sym))))
             (symbol (name sym))))

(defn get-vars-used
  "Return a set of symbols representing all vars used or referenced in
  the given sequence of AST nodes."
  [flat-ast]
  (->> flat-ast
       (filter #(and (= (:op %) :var) (-> % :info :ns)))
       (map #(-> % :info :name))
       set))

(defn extern-for-var [sym]
  (if (= "js" (namespace sym))
    (format "var %s={};\n" (name sym))
    (format "var %s={};\n%s.%s=function(){};\n"
            (namespace sym) (namespace sym) (name sym))))

(defn externs-for-vars [syms]
  (apply str (map extern-for-var syms)))

(defn get-undefined-vars [flat-ast]
  (remove var-defined? (get-vars-used flat-ast)))

(defn externs-for-cljs [file]
  (reset-namespaces!)
  (let [flat-ast (flatten-ast (file-ast file))]
    (str (externs-for-vars (get-undefined-vars flat-ast))
         (externs-for-interop (get-interop-used flat-ast)))))

;; (externs-for-cljs "cljs-example/src/main.cljs")
