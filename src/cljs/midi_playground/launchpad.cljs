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
     :velocity (if-not (= :controller-change t) velocity-or-data)
     :data     (if (= :controller-change t) velocity-or-data)
     :raw msg-array}))
