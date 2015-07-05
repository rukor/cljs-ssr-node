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


(defmacro defhandler
  "Defines a handler to be invoked when a route is matched by the router.

  Usage:

  (defhandler route-id title args & service-calls)

  Where:

    route-id      - the id of the matched route
    title         - a string or function that is used to generate the page title for that route
    args          - vector of parameters (query/path) that should be extracted from the route matcher
    service-calls - a list of pairs of data-key and service calls [data-key service-call]

  A data key may be a keyword, string or vector while a service call may either be a service id (for which
  a com.firstlinq.ssr.api/handle-request multi-method entry has been defined), or a service id and parameters.
  Note that the parameters may use any of the bound items in args.

  For example

  (defhandler :hello \"Greeting\" [name]
    :greeting [:say-hello name])

  defines a handler for route-id :hello, with a page title of \"Greeting\". A single parameter `name` is extracted
  from the route matcher and bound to the symbol `name`, which is then passed as a parameter to the :say-hello service
  call. The result of the [:say-hello name] call is stored in the state map with key :greeting. This is then accessible
  by the views that get rendered throughout the component tree.

  If a service call takes no parameters, then it can simply be represented as a keyword, e.g.

  :greeting :say-hello-world."


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