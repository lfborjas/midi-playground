(ns midi-playground.audio
  "Using the HTML Web Audio API")

;; References

;; https://medium.com/swinginc/playing-with-midi-in-javascript-b6999f2913c3
;; https://www.toptal.com/web/creating-browser-based-audio-applications-controlled-by-midi-hardware
;; https://webaudio.github.io/web-midi-api/#dom-midimessageevent-data

(defn mk-audio-context []
  (if js/window.AudioContext.
    (js/window.AudioContext.)
    (js/window.webkitAudioContext.)))

(defonce context* (mk-audio-context))

(defn midi-to-freq [midi-key]
  (* 440 (Math/pow 2 (/ (- midi-key 69) 12))))

(defn setup-nodes [ctx]
  (let [osc  (.createOscillator ctx)
        gn   (.createGain ctx)
        dst  (.-destination ctx)
        current-time (.-currentTime ctx)]

    
    ;; set up the pipeline: connect the oscillator
    ;; to the gain node, and then the context destination.
    (-> osc
        (.connect gn)
        (.connect dst))

    ;; initial volume: 0
    (-> gn
        (.-gain)
        (.setTargetAtTime 0.0 current-time 0))

    ;; start the oscillator, can only do it once!
    (.start osc)
    
    {:oscillator osc
     :gain gn}))

(defn note-on [ctx {:keys [oscillator gain]} midi-note]
  (let [note (midi-to-freq midi-note)
        current-time (.-currentTime ctx)
        time-constant 0.05]
    ;; see: https://developer.mozilla.org/en-US/docs/Web/API/AudioParam/setTargetAtTime
    (if (= "suspended" ctx)
      (.resume ctx))
    (-> oscillator
        (.-frequency)
        (.setTargetAtTime note current-time time-constant))

    (-> gain
        (.-gain)
        (.setTargetAtTime 0.6 current-time time-constant))))

(defn note-off [ctx {:keys [oscillator gain]}]
  (if (= "suspended" ctx)
    (.resume ctx))

  (-> gain
      (.-gain)
      (.setTargetAtTime 0.0 (.-currentTime ctx) 0))  )
