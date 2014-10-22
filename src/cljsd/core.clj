(ns cljsd.core
  (:require [clojure.string :as s]
            [cljs.env]
            [cljs.compiler :as cljsc]
            [ring.util.response :refer [file-response]])
  (:import [java.util.regex Pattern]))

(defn- unoptimized [path src dest]
  (let [cljs-path (str src (s/replace path #"\.js$" ".cljs"))
        js-path (str dest path)
        opts {:output-dir dest}
        {:keys [file]} (cljs.env/ensure
                         (cljsc/compile-file cljs-path js-path opts))]
    (file-response (.getPath ^java.io.File file))))

(defn wrap [handler {:keys [mount src dest] :as config}]
  (assert (and (string? mount)
               (.startsWith mount "/")
               (.endsWith mount "/")))
  (let [path-re (re-pattern (str "^" (Pattern/quote mount) "(.*)"))]
    (fn [request]
      (if-let [[_ path] (re-find path-re (:uri request))]
        (cond
          ;TODO (.endsWith path ".min.js") (optimized ...)
          (.endsWith path ".js") (unoptimized path src dest)
          :else (handler request)
        (handler request))))))

;TODO prebuild

;;XXX cljsc computes a relative path between an input and output file.
;; Monkey-patch the computed path as if it were between two output files.

;(def add-dep-string cljsc/add-dep-string)
;
;(defn patched-dep-string [& args]
;  (.replace (apply add-dep-string args) "../../../../public/js/" "../"))
;
;(defn build []
;  (mkdirs)
;  (with-redefs [cljsc/add-dep-string patched-dep-string]
;    (cljsc/build "src/proact/client.cljs"
;                 {:libs ["WTF.js"]
;                  :optimizations (if advanced? :advanced :none)
;                  :output-to "target/public/js/proact.js"
;                  :output-dir "target/public/js"
;                  :externs ["public/js/react-0.10.0.js"]
;                  :closure-warnings {:externs-validation :off
;                                     :non-standard-jsdoc :off}})))
;
;(defn repl-env []
;  (weasel/repl-env))
