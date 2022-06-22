(ns portal.jvm-test
  (:require [clojure.test :refer [deftest is]]
            [portal.api :as p]
            [portal.runtime.browser :as browser]
            [portal.runtime.jvm.client :as client]))

(defn- headless-chrome-flags [url]
  ["--headless" "--disable-gpu" url])

(defn- open [f]
  (with-redefs [browser/flags f] (p/open {:mode :test})))

(deftest e2e-jvm
  (when-let [portal (open headless-chrome-flags)]
    (is (some? (some #{portal} (p/sessions))))
    (with-redefs [client/timeout 60000]
      (reset! portal 0)
      (is (= @portal 0))
      (swap! portal inc)
      (is (= @portal 1))
      (is (= 6 (p/eval-str portal "(+ 1 2 3)")))
      (is (= :world (:hello (p/eval-str portal "{:hello :world}")))))
    (p/close portal)
    (is (nil? (some #{portal} (p/sessions))))))
