(ns WebApp.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [clojure.data.json :as json]
              [ring.util.response :as ring-resp]))

(def thingy (atom nil))

(def get-current-time (fn [] (System/currentTimeMillis)))

(defn json-response [obj]
  (json/write-str obj))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defn timer-page [request]
  (if @thingy
    {:status 200 :body {:seconds (- (get-current-time)
                                    @thingy)}}
    {:status 500}))

(defn start-timer-page [request]
  (if-not @thingy
    (do (reset! thingy (get-current-time)) {:status 201  :body {:seconds 0}})
    {:status 500}))

(defn stop-timer [request]
  [request]
  (if @thingy
    (do (reset! thingy nil) {:status 200})
    {:status 500}))



(defroutes routes
  [[["/" {:get home-page}
     ^:interceptors [(body-params/body-params) bootstrap/json-body]
     ["/timer" {:get timer-page, :post start-timer-page, :delete stop-timer}]
     ]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by WebApp.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              :a :b

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
