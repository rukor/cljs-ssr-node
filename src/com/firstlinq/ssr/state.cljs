(ns com.firstlinq.ssr.state
  (:require [cognitect.transit :as t]))

(defmulti get-state (fn [state route-id route-params opts] route-id))

(defprotocol Serialiser
  (->string [_ state])
  (->state [_ string]))

(defn transit-serialiser []
  (reify Serialiser
    (->string [_ state]
      (let [w (t/writer :json)]
        (t/write w state)))

    (->state [_ string]
      (let [r (t/reader :json)]
        (t/read r string)))))

(defn hydrate [serialiser state-atom el]
  (when (empty? @state-atom)
    (->> (.-textContent el)
         (->state serialiser)
         (reset! state-atom))))