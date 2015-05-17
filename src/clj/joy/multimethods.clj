(ns joy.multimethods)

(defmacro with-prn [& body]
  `(try (let [x# (do ~@body)] (prn x#) x#) (catch Throwable t# (prn t#))))

(defn beget [this proto]
  (assoc this ::prototype proto))

(defn get [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (::prototype m) k))))

(get (beget {:sub 0} {:super 1}) :super) ;=> 1

(def put assoc)

(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx [m] (get m :llvm-compiler))

(def clone (partial beget {}))
(def unix {:os ::unix, :c-compiler "cc", :home "/home", :dev "/dev"})
(def osx (-> (clone unix)
             (put :os ::osx)
             (put :llvm-compiler "clang")
             (put :home "/Users")))

(with-prn (compiler unix))
;=> "cc"
(with-prn (compiler osx))
;=> "clang"

(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(with-prn (home unix))
;=> "/home"

(with-prn (home osx))
; java.lang.IllegalArgumentException: No method in multimethod 'home' for dispatch value: :user/osx

(derive ::osx ::unix)

(with-prn (home osx))
;=> "/Users"
(with-prn (parents ::osx))
;=> #{:user/unix}
(with-prn (ancestors ::osx))
;=> #{:user/unix}
(with-prn (descendants ::unix))
;=> #{:user/osx}
(with-prn (isa? ::osx ::unix))
;=> true
(with-prn (isa? ::unix ::osx))
;=> false

(derive ::osx ::bsd)
(defmethod home ::bsd [m] "/home")

(with-prn (home osx))
; java.lang.IllegalArgumentException: Multiple methods in multimethod
;  'home' match dispatch value: :user/osx -> :user/unix and
;  :user/bsd, and neither is preferred

(prefer-method home ::unix ::bsd)

(with-prn (home osx)) ;; ::unix version is called
;=> "/Users"

(remove-method home ::bsd)

(with-prn (home osx))
;=> "/Users"

(with-prn (derive (make-hierarchy) ::osx ::unix))
;=> {:parents {:user/osx #{:user/unix}},:ancestors {:user/osx #{:user/unix}},:descendants {:user/unix #{:user/osx}}}

(defmulti compile-cmd (juxt :os compiler)) ;; juxt builds a vector (see sidebar)

(defmethod compile-cmd [::osx "clang"] [m]   ;; Match the vector exactly
  (str "/usr/bin/" (get m :c-compiler)))

(defmethod compile-cmd :default [m]
  (str "Unsure where to locate " (get m :c-compiler)))

(with-prn (compile-cmd osx))
;=> "/usr/bin/cc"

(with-prn (compile-cmd unix))
;=> "Unsure where to locate cc"
