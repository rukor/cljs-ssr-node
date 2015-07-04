(ns com.firstlinq.ssr.routes)

(defn- ->service-vector [service-id]
  (if (vector? service-id)
    [(first service-id)
     (second service-id)
     (second (rest service-id))]
    [service-id nil nil]))

(defn- request-vecs [service-calls]
  (for [[data-key service-id] (partition 2 service-calls)
        :let [[svc-call-id param txform] (->service-vector service-id)]]
    [data-key svc-call-id param txform]))

(defn- make-req-map [state-sym service-reqs]
  (reduce
    (fn [accum [data-key service-call param txform]]
      (->> `[(com.firstlinq.ssr.api/handle-request ~state-sym ~service-call ~param) ~txform]
           (assoc accum data-key)))
    {}
    service-reqs))

(defn- make-redirect-check [state-sym opts]
  `(when-let [r-fn# (:redirect? ~opts)]
     (r-fn# ~state-sym)))


(defmacro defroute
  ""
  [route-id title args & service-calls]
  (let [[opts service-calls] (if (map? (first service-calls))
                               [(first service-calls) (rest service-calls)]
                               [nil service-calls])
        state-sym    (gensym "state")
        service-reqs (request-vecs service-calls)
        req-map      (make-req-map state-sym service-reqs)]
    `(defmethod com.firstlinq.ssr.state/get-state ~route-id
       [~state-sym _# params# opts#]
       (let [{:keys ~args} params#
             redirect-uri# ~(make-redirect-check state-sym opts)
             init-state#   (cond-> ~state-sym redirect-uri# ; init state
                                   (assoc com.firstlinq.ssr/redirect-key redirect-uri#))
             req-map#      (if-not redirect-uri# ~req-map {})
             route#        {:id ~route-id :params params#}]
         (com.firstlinq.ssr.routes/reify-map init-state# req-map# route# ~title ~opts)))))