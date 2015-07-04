(ns com.firstlinq.ssr.api
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan <! >!]]
            [com.firstlinq.ssr.log :as log :include-macros true]))

(defmulti handle-request (fn [state id params] id))

(defmethod handle-request :default [_ id _]
  (log/warn "No service handler registered for " id))

(defprotocol Service
  (call [this service payload]
        "Calls a service api and returns a channel through which the result will be returned."))

(defrecord AService [in-ch]
  Service
  (call [_ service params]
    (let [return-ch (chan)]
      (go (>! in-ch {:service   service
                     :params    params
                     :return-ch return-ch}))
      return-ch)))

(defn create-service
  "Creates a service instance."
  [state]
  (log/debug "Initialising service")
  (let [ch  (chan)
        svc (map->AService {:in-ch ch})]
    (go-loop []
      (log/debug "Waiting for a request")
      (when-let [{:keys [service params return-ch]} (<! ch)]
        (log/debug "Received request " service " with params " params ". Executing handler...")
        (go (>! return-ch (<! (handle-request @state service params))))
        (recur)))
    (log/debug "Done.")
    svc))