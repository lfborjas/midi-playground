(ns midi-playground.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::devices
 (fn [db _]
   (:devices db)))

(re-frame/reg-sub
 ::input-device
 (fn [db _]
   (:input-device db)))

(re-frame/reg-sub
 ::output-device
 (fn [db _]
   (:output-device db)))
