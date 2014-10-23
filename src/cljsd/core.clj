(ns cljsd.core
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [cljs.env]
            [cljs.closure :as cljsc]
            [ring.util.response :refer [file-response resource-response]])
  (:import [java.util.regex Pattern]
           [java.io File]))

(defn- unoptimized [{:keys [src dest] :as config} path]
  (let [cljs-path (str src (s/replace path #"\.js$" ".cljs"))
        js-path (str dest path)
        opts {:output-dir dest}]
    (or ;; ClojureScript
        (let [^File cljs-file (File. cljs-path)]
          (when (.exists cljs-file)
            (cljsc/build cljs-file {:optimizations :none
                                    :output-to js-path
                                    :output-dir dest})
            (file-response js-path)))
        ;; JavaScript file
        (let [^File js-file (File. js-path)]
          (when (.exists js-file)
            (file-response js-path)))
        ;; Javascript resource
        ;;TODO Do I need/want this? cljsc/build copies goog files.
        (when-let [js-resource (io/resource path)]
          (resource-response path))
        ;; Not found
        {:status 404
         :headers {"Content-Type" "text/plain"}
         :body (str "No cljs or js found for " path)})))

(defn wrap [handler {:keys [mount src dest] :as config}]
  (assert (and (string? mount)
               (.startsWith mount "/")
               (.endsWith mount "/")))
  (let [path-re (re-pattern (str "^" (Pattern/quote mount) "(.*)"))]
    (fn [request]
      (if-let [[_ path] (re-find path-re (:uri request))]
        (cond
          ;TODO (.endsWith path ".min.js") (optimized ...)
          (.endsWith path ".js") (unoptimized config path)
          :else (handler request))
        (handler request)))))

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
