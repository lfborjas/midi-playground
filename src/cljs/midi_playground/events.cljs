(ns midi-playground.events
  (:require
   [re-frame.core :as re-frame]
   [midi-playground.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   ))

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
