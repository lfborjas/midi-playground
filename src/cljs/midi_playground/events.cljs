(ns midi-playground.events
  (:require
   [re-frame.core :as re-frame]
   [midi-playground.db :as db]
   [midi-playground.launchpad :as launchpad]
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

(re-frame/reg-fx
 ::set-led-color
 (fn [[device key]]
   (launchpad/flash-on-press device key :full-green)))

(re-frame/reg-event-fx
 ::parse-incoming-message
 (fn-traced [{:keys [db]} [_ msg]]
            (let [parsed (launchpad/parse-input-message msg)]
              {:db  (update-in db
                               [:raw-input-messages]
                               (fnil conj [])
                               parsed)
               ;; TODO: need an interceptor for the output device?
               ::set-led-color [(:output-device db)
                                (:key parsed)]})))
