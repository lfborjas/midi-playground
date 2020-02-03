(ns midi-playground.events
  (:require
   [re-frame.core :as re-frame]
   [midi-playground.db :as db]
   [midi-playground.launchpad :as launchpad]
   [midi-playground.audio :as audio]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
            (assoc db :active-panel active-panel)))


(re-frame/reg-event-db
 ::set-input-device
 (fn-traced [db [_ device]]
            (assoc db :input-device device)))

(re-frame/reg-event-db
 ::set-output-device
 (fn-traced [db [_ device]]
            (assoc db :output-device device)))

(re-frame/reg-event-db
 ::setup-audio-pipeline
 (fn-traced [db [_]]
            (assoc db :audio-nodes (audio/setup-nodes audio/context*))))

(re-frame/reg-fx
 ::key-feedback
 (fn [[device msg]]
   (if (launchpad/is-key-press? msg)
     (launchpad/set-key-color device msg :yellow)
     (launchpad/turn-key-off  device msg))))

(re-frame/reg-fx
 ::play-note
 (fn [[audio-nodes {:keys [key] :as msg}]]
   (if (launchpad/is-key-press? msg)
     (audio/note-on audio/context*
                    audio-nodes
                    key)
     (audio/note-off audio/context*
                     audio-nodes))))

(re-frame/reg-fx
 ::change-layout!
 (fn [[device layout]]
   (launchpad/set-mapping-mode device layout)))

(re-frame/reg-event-fx
 ::parse-incoming-message
 (fn-traced [{:keys [db]} [_ msg]]
            (let [parsed (launchpad/parse-input-message msg)]
              {:db  (update-in db
                               [:raw-input-messages]
                               (fnil conj [])
                               parsed)
               ;; TODO: need an interceptor for the output device?
               ::key-feedback [(:output-device db)
                               parsed]
               ::play-note    [(:audio-nodes db)
                               parsed]})))


(re-frame/reg-event-fx
 ::set-layout
 (fn-traced [{:keys [db]} [_ layout]]
            {:db (assoc db :current-layout layout)
             ::change-layout! [(:output-device db)
                               layout]}))
