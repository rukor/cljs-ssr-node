(ns com.firstlinq.ssr.log)

(defmacro do-log [level & msg]
  (let [prefix `(str (js/Date.) " " ~level)
        msg    (map (fn [x] `(com.firstlinq.ssr.log/->log-output ~x)) msg)]
    (condp = level
      :info `(when js/console (.log js/console ~prefix ~@msg))
      :debug `(when js/console (.debug js/console ~prefix ~@msg))
      :warn `(when js/console (.warn js/console ~prefix ~@msg))
      :error `(when js/console (.error js/console ~prefix ~@msg))
      )))

(defmacro info [& msg] `(do-log :info ~@msg))

(defmacro debug [& msg] `(do-log :debug ~@msg))

(defmacro warn [& msg] `(do-log :warn ~@msg))

(defmacro error [& msg] `(do-log :error ~@msg))
