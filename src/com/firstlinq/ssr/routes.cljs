(ns com.firstlinq.ssr.routes
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan put!]]
            [com.firstlinq.ssr :refer [redirect-key]]))

(defn- append-return-uri [uri return-uri]
  (when uri
    (let [return-uri (js/encodeURIComponent return-uri)]
      (if (not= -1 (.indexOf uri "?"))
        (str uri "&redirect=" return-uri)
        (str uri "?redirect=" return-uri)))))

(defn- add-title-to-state [title state]
  (cond-> state
          (fn? title)
          (assoc-in [:route :title] (title state))

          (string? title)
          (assoc-in [:route :title] title)))

(defn create-route-handler [state]
  (fn [id params]
    (let [params (->> (get-in params [:domkm.silk/url :query])
                      (assoc params :query))
          uri    (str (:domkm.silk/url params))]
      (go
        (when-let [ch (com.firstlinq.ssr.state/get-state @state id params {})]
          (let [new-state (<! ch)]
            (if-let [location (redirect-key new-state)]
              (do (set! (.-location js/window) (append-return-uri location uri)))
              (do (swap! state merge new-state)
                  (when-let [title (get-in new-state [:route :title])]
                    (set! (.-title js/document) title))))))))))

(defn reify-map
  [init req-map route title opts]
  (let [ch (chan)]
    (go
      (loop [accum (merge init {:route route})
             items req-map]
        (if-let [item (first items)]
          (let [[k [v txform]] item
                res (<! v)
                res (if txform (txform (:body res)) (:body res))
                res (if (vector? k) (assoc-in accum k res) (assoc accum k res))]
            (recur res (rest items)))
          (put! ch (add-title-to-state title accum)))))
    ch))