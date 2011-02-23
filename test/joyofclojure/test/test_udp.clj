(ns joyofclojure.test.test-udp
  (:use [clojure.test :only [deftest is]])
  (:refer-clojure :exclude [get])
  (:use joy.udp :reload))


(def cat {:likes-dogs true, :ocd-bathing true})
(def morris (beget {:likes-9lives true} cat))
(def post-traumatic-morris (beget {:likes-dogs nil} morris))

(deftest test-get
  (is (get cat :likes-dogs))
  (is (get morris :likes-dogs))
  (is (not (get post-traumatic-morris :likes-dogs))))


(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx [m] (get m :c-compiler))


(def unix   {:os ::unix, :c-compiler "cc", :home "/home", :dev "/dev"})
(def osx  (-> (clone unix) (put :os ::osx) (put :c-compiler "gcc") (put :home "/Users")))

(deftest test-proto-lookup-simple
  (is (= "cc"  (compiler unix)))
  (is (= "gcc" (compiler osx))))


(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(deftest test-proto-lookup-derived
  (is (= "/home" (home unix)))
  (is (thrown? IllegalArgumentException (home osx)))
  (is (= "/Users" (do
                    (derive ::osx ::unix)
                    (home osx))))
  (is (isa? ::osx ::unix)))


(defmethod home ::bsd [m] "/home")

(deftest test-proto-lookup-derived-prefer
  (is (thrown? IllegalArgumentException (do (derive ::osx ::bsd)
                                            (home osx))))
  (is (= "/Users" (do (prefer-method home ::unix ::bsd)
                      (home osx))))
  (is (= "/home"  (home {:os ::bsd})))
  (is (thrown? IllegalArgumentException (do (remove-method home ::bsd)
                                            (home {:os ::bsd}))))
  (is (= "/Users" (home osx))))
