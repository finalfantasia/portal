(ns portal.ui.viewer.http
  (:require [clojure.spec.alpha :as sp]
            [clojure.string :as str]
            [portal.colors :as c]
            [portal.ui.inspector :as ins]
            [portal.ui.select :as select]
            [portal.ui.styled :as s]
            [portal.ui.theme :as theme]))

(sp/def ::method #{:get     "GET"
                   :head    "HEAD"
                   :post    "POST"
                   :put     "PUT"
                   :patch   "PATCH"
                   :delete  "DELETE"
                   :options "OPTIONS"})

(sp/def ::uri string?)
(sp/def ::url string?)

(defn valid-status? [value] (<= 100 value 599))
(sp/def ::status (sp/and int? valid-status?))
(sp/def ::name (sp/or :string string? :keyword keyword?))
(sp/def ::header (sp/or :string string? :strings (sp/coll-of string?)))
(sp/def ::headers (sp/map-of ::name ::header))
(sp/def ::query-params (sp/map-of ::name ::name))
(sp/def ::doc string?)

(sp/def ::request-method #{:get :head :post :put :patch :delete})

(sp/def ::request
  (sp/keys :req-un [::request-method
                    ::uri]
           :opt-un [::headers
                    ::query-params
                    ::body
                    ::doc]))

(sp/def ::request
  (sp/keys :req-un [::request-method
                    ::uri]
           :opt-un [::headers
                    ::query-params
                    ::body
                    ::doc]))

(sp/def ::response
  (sp/keys :req-un [::status]
           :opt-un [::headers
                    ::body
                    ::doc]))

(def ^:private method->color
  {:get     ::c/boolean
   :head    ::c/number
   :put     ::c/uri
   :post    ::c/string
   :patch   ::c/namespace
   :delete  ::c/exception
   :options ::c/package})

(defn inspect-http-request [value]
  (let [theme      (theme/use-theme)
        opts       (ins/use-options)
        background (ins/get-background)
        color      (-> value :request-method method->color theme)
        expanded?  (:expanded? opts)]
    [s/div
     [s/div
      {:style
       {:display     :flex
        :align-items :stretch
        :background  background}}
      [s/div
       {:style
        {:cursor     :pointer
         :color      background
         :padding    [(:padding theme) (* 2.5 (:padding theme))]
         :background color
         :border     [1 :solid color]

         :border-top-left-radius (:border-radius theme)
         :border-bottom-left-radius
         (when-not expanded? (:border-radius theme))}}
       (str/upper-case (name (:request-method value)))]
      [s/div
       {:style
        {:flex    "1"
         :padding [(:padding theme) (* 2 (:padding theme))]
         :border  [1 :solid (::c/border theme)]

         :border-bottom-style     (when expanded? :none)
         :border-top-right-radius (:border-radius theme)
         :border-bottom-right-radius
         (when-not expanded? (:border-radius theme))}}
       [select/with-position
        {:row -1 :column 0}
        [ins/with-key
         :uri
         [ins/inspector (:uri value)]]]]]
     (when (:expanded? opts)
       [ins/inspect-map-k-v (dissoc value :uri :request-method)])]))

(defn- status->color [status]
  (cond
    (<= 100 status 199) ::c/boolean
    (<= 200 status 299) ::c/string
    (<= 300 status 399) ::c/tag
    (<= 400 status 499) ::c/exception
    (<= 500 status 599) ::c/exception))

(defn inspect-http-response [value]
  (let [theme        (theme/use-theme)
        opts         (ins/use-options)
        expanded?    (:expanded? opts)
        background   (ins/get-background)
        content-type (or (get-in value [:headers "Content-Type"])
                         (get-in value [:headers :content-type]))
        color        (-> value :status status->color theme)]
    [s/div
     [s/div
      {:style
       {:display    :flex
        :background background}}
      [s/div
       {:style
        {:cursor                 :pointer
         :background             color
         :padding                [(:padding theme) (* 2.5 (:padding theme))]
         :color                  background
         :border                 [1 :solid color]
         :border-top-left-radius (:border-radius theme)
         :border-bottom-left-radius
         (when-not expanded? (:border-radius theme))}}
       (:status value)]
      [s/div
       {:style
        {:flex                    "1"
         :padding                 (:padding theme)
         :border                  [1 :solid (::c/border theme)]
         :border-bottom-style     (when expanded? :none)
         :border-top-right-radius (:border-radius theme)
         :border-bottom-right-radius
         (when-not expanded? (:border-radius theme))}}
       [select/with-position
        {:row -1 :column 0}
        [ins/with-key
         :headers
         [ins/with-key
          "Content-Type"
          [ins/inspector content-type]]]]]]
     (when (:expanded? opts)
       [ins/inspect-map-k-v (dissoc value :status)])]))

(defn get-component [value]
  (cond
    (sp/valid? ::request value)
    inspect-http-request

    (sp/valid? ::response value)
    inspect-http-response))

(defn inspect-http [value]
  (let [component (get-component value)]
    [component value]))

(def viewer
  {:predicate get-component
   :component inspect-http
   :name      :portal.viewer/http})
