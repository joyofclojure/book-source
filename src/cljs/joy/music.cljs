(ns joy.music
  "Functions for interoperating with the Web Audio API"
  (:require [joy.tunes :as tunes]))

(defn soft-attack
  "Return a gain node that goes from silent at time <delay> up to
  <volume> in 50 milliseconds, then ramps back down to silent after
  <duration>"
  [ctx {:keys [volume delay duration]}]
  (let [node (.createGainNode ctx)]    ;; createGain
    (doto (.-gain node)
      (.linearRampToValueAtTime 0 delay)
      (.linearRampToValueAtTime volume (+ delay 0.05))
      (.linearRampToValueAtTime 0 (+ delay duration)))
    node))

(defn sine-tone
  "Return an oscillator that plays starting at <delay> for <duration> seconds"
  [ctx {:keys [cent delay duration]}]
  (let [node (.createOscillator ctx)]
    (set! (-> node .-frequency .-value) 440)
    (set! (-> node .-detune .-value) (- cent 900))
    (.noteOn node delay)
    (.noteOff node (+ delay duration))
    node))

(defn connect-to
  "Connect the output of node1 to the input of node2, returning node2"
  [node1 node2]
  (.connect node1 node2)
  node2)

(defn woo
  "Play a 'woo' sound; sounds a bit like a glass harp."
  [ctx note]
  (let [linger 1.5
        note (update-in note [:duration] * linger)]
    (-> (sine-tone ctx note)
        (connect-to (soft-attack ctx note)))))

(defn play!
  "Kick off playing a sequence of notes. note-fn must take two
  arguments, an AudioContext object and a map representing one note to
  play. It must return an AudioNode object that will play that note."
  [note-fn notes]
  (if-let [ctor (or (.-AudioContext js/window)
                      (.-webkitAudioContext js/window))]
    (let [ctx (new ctor)
          compressor (.createDynamicsCompressor ctx)] ;; for the safety of your speakers and ears
      (let [now (.-currentTime ctx)]
        (doseq [note notes]
          (->
           (note-fn ctx (update-in note [:delay] + now))
           (connect-to compressor))))
      (connect-to compressor (.-destination ctx)))
    (js/alert "Sorry, this browser doesn't seem to support AudioContext")))

#_(play! woo [{:cent 1100, :duration 1, :delay 0, :volume 0.4}])

#_(play! woo [{:cent 1100, :duration 1, :delay 0.0, :volume 0.4}
              {:cent 1400, :duration 1, :delay 0.2, :volume 0.4}
              {:cent 1800, :duration 1, :delay 0.4, :volume 0.4}])


(defn ^:export go []
  (play! woo tunes/magical-theme))

;; Uncomment to begin playing on page load
;; (go)
