(ns com.firstlinq.ssr.server
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.nodejs :as node]
            [com.firstlinq.ssr.state :refer [->string ->state transit-serialiser]]
            [com.firstlinq.ssr.util :refer [->ring-req]]
            [cljs.core.async :refer [<!]]))


(defn make-plates-template
  "Trivial template"
  [serialiser template-file]
  (let [fs         (node/require "fs")
        plates     (node/require "plates")
        index-html (.readFileSync fs template-file "utf-8")]
    (fn [state rendered]
      (.bind plates index-html #js{:app       rendered
                                   :app-state (->string serialiser state)}))))

(defn make-renderer
  "Trivial renderer"
  [render req->state template]
  (fn [req res]
    (let [req (->ring-req req)]
      (print "Request: " req)
      (go
        (let [state-ch (req->state req)
              state    (when state-ch (<! state-ch))]
          (if state
            (do
              (print "Handling: " (:uri req))
              (->> (render state)
                   (template state)
                   (.send res)))
            (do (print "No state returned for " (:uri req))
                (.send res "Not found" 404))))))))


(defn start [& {:keys [renderer template-file request->state serialiser
                       docroot port]
                :or   {serialiser (transit-serialiser)
                       docroot    "resources/public"
                       port       3000}}]
  (let [express  (node/require "express")                   ; load express
        app      (express)

        st       (node/require "st")                        ; load st module
        mount    (st #js{:path        docroot
                         :url         "/"
                         :index       false
                         :passthrough true})

        template (make-plates-template serialiser template-file)
        renderer (make-renderer renderer request->state template)
        ]

    (doto app
      (.use mount)
      (.get #".*" renderer)
      (.listen port))
    (println (str "Server started on port: " port))

    ;; return stop fn
    (fn []
      (println "Stopping server")
      (.stop app))))
