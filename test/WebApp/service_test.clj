(ns WebApp.service-test
  (:require 
    [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [clojure.data.json :as json]
            [WebApp.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))


(deftest get-time-test
  (testing "error when timer is not started"
  (with-redefs [service/thingy (atom nil)]
    (is (= (:status (response-for service :get "/timer")) 500)))))

(deftest start-timer-test
  (testing "ok when timer is started."
    (let [response (response-for service :post "/timer")]
         (is (= (:status response) 201))
         (is (= (:body response) (json/write-str {:seconds 0})))))

  (testing "Starting a stated timer is an error"
    (with-redefs [service/thingy (atom nil)]
    (let [service (::bootstrap/service-fn (bootstrap/create-servlet service/service))
          first_response (response-for service :post "/timer")
          second_response (response-for service :post "/timer")]
         (is (= (:status first_response) 201))
         (is (= (:status second_response) 500)))))

  (testing "After a timer is started, getting the time returns 200"
      (with-redefs [service/thingy (atom nil)
                    service/get-current-time (constantly 5)]
        (let [service (::bootstrap/service-fn (bootstrap/create-servlet service/service))
              start_response (response-for service :post "/timer")
          
              get_response (with-redefs [service/get-current-time (constantly 15)]
                          (response-for service :get "/timer"))]
         (is (= (:status get_response) 200))
         (is (= (:body get_response) (json/write-str {:seconds 10}))))))

  (testing "After a timer is started, getting the time returns 200"
      (with-redefs [service/thingy (atom nil)
                    service/get-current-time (constantly 5)]
        (let [service (::bootstrap/service-fn (bootstrap/create-servlet service/service))
              start_response (response-for service :post "/timer")
          
              get_response (with-redefs [service/get-current-time (constantly 25)]
                          (response-for service :get "/timer"))]
         (is (= (:status get_response) 200))
         (is (= (:body get_response) (json/write-str {:seconds 20}))))))

  (testing "Stop running timer"
      (with-redefs [service/thingy (atom nil)
                    service/get-current-time (constantly 5)]
        (let [service (::bootstrap/service-fn (bootstrap/create-servlet service/service))
              post_response (response-for service :post "/timer")
              delete_response (response-for service :delete "/timer")
              get_response (response-for service :get "/timer")]
         (is (= (:status delete_response) 200))
         (is (= (:status get_response) 500))))))
