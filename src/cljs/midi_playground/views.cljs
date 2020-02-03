(ns midi-playground.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [re-com.core :as re-com]
   [midi-playground.subs :as subs]
   [midi-playground.events :as events]))


;; Some references:
;; https://webaudiodemos.appspot.com/midi-synth/index.html
;; https://www.onlinemusictools.com/webmiditest/

(defn home-title []
  (let [name (rf/subscribe [::subs/name])]
    [re-com/title
     :label (str "MIDI Playground.")
     :level :level1]))

(defn link-to-about-page []
  [re-com/hyperlink-href
   :label "go to About Page"
   :href "#/about"])

(defn- parse-devices! [midi]
  (let [first-input (if (pos? (.. midi -inputs -size))
                      (.. midi -inputs values next -value))
        first-output (if (pos? (.. midi -outputs -size))
                       (.. midi -outputs values next -value))]
    (when (and first-input first-output)
      (rf/dispatch [::events/set-input-device first-input])
      (rf/dispatch [::events/set-output-device first-output])
      (rf/dispatch [::events/setup-audio-pipeline])
      (set! (.-onmidimessage first-input)
            #(rf/dispatch [::events/parse-incoming-message %])))))

(defn- setup-midi-devices! [midi]
  (-> midi
      parse-devices!)
  (set! (.-onstatechange midi) #(parse-devices! (.-target %))))

(defn- refresh-device! []
  (-> js/navigator .requestMIDIAccess (.then setup-midi-devices!)))

(defn device-search-button []
  [re-com/button
   :label "Setup MIDI device"
   :on-click refresh-device!])

(defn devices-info [i o]
  [:div
   (if i
     [re-com/alert-box
      :id "input-device"
      :alert-type :info
      :heading "Input device"
      :body (.-name i)])
   (if o
     [re-com/alert-box
      :id "output-device"
      :alert-type :info
      :heading "Output device"
      :body (.-name o)])])

(defn messages-display [messages]
  [:div
   (for [msg messages]
     ^{:key (random-uuid)}
     [:<>
      [:pre (str msg)]])])

(defn toggle-layout-button [current-layout]
  (let [set-layout #(if (= :x-y current-layout)
                      (rf/dispatch [::events/set-layout :drum-rack])
                      (rf/dispatch [::events/set-layout :x-y]))
        layout (if (= :x-y current-layout)
                 "X-Y Layout"
                 "Drum Machine Layout")]
    [re-com/button
     :label layout
     :on-click set-layout]))

(defn home-panel []
  (let [input (rf/subscribe [::subs/input-device])
        output (rf/subscribe [::subs/output-device])
        messages (rf/subscribe [::subs/input-messages])
        layout   (rf/subscribe [::subs/input-layout])]
    [re-com/v-box
     :gap "1em"
     :children [[home-title]
                [re-com/h-box
                 :children [[device-search-button]
                            [toggle-layout-button @layout]]]
                [devices-info @input @output]
                [messages-display @messages]]]))


;; about

(defn about-title []
  [re-com/title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [re-com/v-box
   :gap "1em"
   :children [[about-title]
              [link-to-home-page]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :children [[panels @active-panel]]]))
