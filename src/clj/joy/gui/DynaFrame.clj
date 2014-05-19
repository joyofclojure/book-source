(ns joy.gui.DynaFrame
  "The dynamic frame class example from section 10.2"
  (:gen-class
   :name         joy.gui.DynaFrame
   :extends      javax.swing.JFrame
   :prefix       df-
   :implements   [clojure.lang.IMeta]
   :state        state
   :init         init
   :constructors {[String] [String]
                  [] [String]}
   :methods      [[display [java.awt.Container] void]
                  #^{:static true} [version [] String]])
  (:import (javax.swing JFrame JPanel JComponent)
           (java.awt BorderLayout Container)))


(defn df-init [title]
  [[title] (atom {::title title})])

(defn df-meta [this] @(.state this))

(defn version [] "1.0")

(defn df-display [this pane]
  (doto this
    (-> .getContentPane .removeAll)
    (.setContentPane (doto (JPanel.)
                       (.add pane BorderLayout/CENTER)))
    (.pack)
    (.setVisible true)))
