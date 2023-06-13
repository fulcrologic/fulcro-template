(ns app.sample-test
  (:require
    [clojure.test :refer [deftest]]
    [fulcro-spec.core :refer [specification provided behavior assertions]]))

; Tests for both client and server
(deftest sample-test
  (behavior "addition computes addition correctly"
    (assertions
      "with positive integers"
      (+ 1 5 3) => 9
      "with negative integers"
      (+ -1 -3 -5) => -9
      "with a mix of signed integers"
      (+ +5 -3) => 2)))
