(ns midi-playground.launchpad
  "Implementation of the message spec for Launchpad MK1

  See doc/launchpad-programmers-reference.pdf")

(defn parse-input-message
  "Parse a message array as a map.

  There's two types of input messages for the Launchpad:

  * Grid button pressed: [144, Key, Velocity]
  * Automap/Live (controller): [176, 104-111, Data]

  In both cases, the third byte (velocity or data) is either 0 or 127,
  upon release and press, respectively."
  [raw-msg]
  (let [msg-array (-> raw-msg
                      .-data
                      ;; the data is a raw native array:
                      (array-seq))
        [message-type key velocity-or-data] msg-array
        
        t (case message-type
            128 :note-off ;; in practice, the Launchpad never sends note-off.
            144 :note-on
            176 :controller-change)]
    {:message-type t
     :key key
     :velocity velocity-or-data
     :data     (if (= :controller-change t) velocity-or-data)
     :raw msg-array}))

(defn turn-led-off
  "Turn a given key off.

  As per the reference, we include the last value (`velocity`)
  but it is actually ignored."
  [device key]
  (.send device [128 key 127]))

(def color->velocity
  {:off 12
   :low-red 13
   :full-red 15
   :low-amber 29
   :full-amber 63
   :yellow 62
   :low-green 28
   :full-green 60
   :flashing-red 11
   :flashing-amber 59
   :flashing-yellow 58
   :flashing-green 56})

(defn set-note-color
  "Turn a given key on, with the specified light behavior.

  The key must be the actual number corresponding to the currently selected
  layout."

  [device key color-alias]
  (.send device [144 key (color->velocity color-alias)]))

(defn set-control-color
  "Turn on a given control key, with the specified light behavior."
  [device key color-alias]
  (.send device [176 key (color->velocity color-alias)]))

(defn is-control-key? [{:keys [key]}]
  (<= 104 key 111))

(defn is-key-release? [{:keys [velocity]}]
  (zero? velocity))

(defn is-key-press? [{:keys [velocity]}]
  (= 127 velocity))

(defn set-key-color
  [device {:keys [key] :as msg} color-alias]
  (if (is-control-key? msg)
    (set-control-color device key color-alias)
    (set-note-color     device key color-alias)))

(defn turn-key-off
  [device {:keys [key] :as msg}]
  (if (is-control-key? msg)
    (set-control-color device key :off)
    (turn-led-off device key)))

(defn reset
  "All LEDs turn off, mapping mode, buffer settings and duty cycle are reset."
  [device]
  (.send device [176 0 0]))

(defn set-mapping-mode
  "Set mode to `:x-y` (default) or `:drum-rack`"
  [device mode]
  (.send device [176 0 (case mode
                         :x-y 1
                         :drum-rack 2
                         1)]))

(defn turn-on-all-leds
  "Turns on all LEDs, with a given brightness"
  [device brightness]
  (.send device [176 0 (case brightness
                         :low 125
                         :medium 126
                         :full 127)]))
