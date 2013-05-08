(ns WebApp.service-test
  (:require 
    [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [clojure.data.json :as json]
            [WebApp.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest home-page-test
  (is (=
       (:body (response-for service :get "/"))
       "Hello World!"))
  (is (=
       (:headers (response-for service :get "/"))
       {"Content-Type" "text/html"})))

(deftest about-page-test
  (is (.contains
       (:body (response-for service :get "/about"))
       "Clojure 1.5"))
  (is (=
       (:headers (response-for service :get "/about"))
       {"Content-Type" "text/html"})))

(deftest get-time-test
  (testing "error when timer is not started"
    (is (= (:status (response-for service :get "/timer")) 500))))

(deftest start-timer-test
  (testing "ok when timer is started."
    (let [response (response-for service :post "/timer")]
         (is (= (:status response) 201))
         (is (= (:body response) (json/write-str {:seconds 0})))))
  (testing "ok when timer is started."
    (with-redefs [service/thingy (atom nil)]
    (let [service (::bootstrap/service-fn (bootstrap/create-servlet service/service))
          first_response (response-for service :post "/timer")
          second_response (response-for service :post "/timer")]
         (is (= (:status first_response) 201))
         (is (= (:status second_response) 500))))))
