(ns app.ui.root
  (:require
    [app.model.session :as session]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]
    [taoensso.timbre :as log]))


(defsc Signup [this {:account/keys [email password password-again]}]
  {:query         [:account/email :account/password :account/password-again]
   :initial-state {:account/email          ""
                   :account/password       ""
                   :account/password-again ""}
   :ident         (fn [] [:screen/id :signup])
   :route-segment ["signup"]
   :will-enter    (fn [_ _]
                    (dr/route-immediate [:screen/id :signup]))}
  (div
    (dom/h3 "Signup")
    (div :.ui.form
      (div :.two.fields
        (div :.ui.field.row
          (dom/label {:htmlFor "email"} "Email")
          (dom/input {:name     "email" :value (or email "")
                      :onChange #(m/set-string! this :account/email :event %)}))
        (div :.ui.field.row
          (dom/label {:htmlFor "password"} "Password")
          (dom/input {:type     "password" :name "password" :value (or password "")
                      :onChange #(m/set-string! this :account/password :event %)}))
        (div :.ui.field.row
          (dom/label {:htmlFor "password2"} "Repease Password")
          (dom/input {:type     "password" :name "password2" :value (or password-again "")
                      :onChange #(m/set-string! this :account/password-again :event %)}))))))

(declare Session)

(defsc Login [this {:account/keys [email]
                    :root/keys    [current-session]
                    :ui/keys      [error open?] :as props}]
  {:query         [:ui/open? :ui/error :account/email
                   {[:root/current-session '_] (comp/get-query Session)}
                   [::uism/asm-id ::session/session]]
   :initial-state {:account/email "" :ui/error ""}
   :ident         (fn [] [:component/id :login])}
  (let [current-state (uism/get-active-state this ::session/session)
        {current-user :account/name} current-session
        loading?      (= :state/checking-session current-state)
        logged-in?    (= :state/logged-in current-state)
        password      (or (comp/get-state this :password) "")] ; c.l. state for security
    (dom/div
      (dom/div current-user (if logged-in?
                              (dom/button :.ui.button
                                {:onClick #(uism/trigger! this ::session/session :event/logout)}
                                "Log out")
                              (dom/div
                                (dom/a
                                  {:onClick #(uism/trigger! this ::session/session :event/toggle-modal)}
                                  "Login"))))
      (when open?
        (dom/div
          (dom/h3 :.ui.header "Login")
          (div :.ui.form {:classes [(when (seq error) "error")]}
            (div :.ui.field
              (dom/label {:htmlFor "email"} "Email")
              (dom/input {:name     "email"
                          :value    email
                          :onChange #(m/set-string! this :account/email :event %)}))
            (div :.ui.field
              (dom/label {:htmlFor "password"} "Password")
              (dom/input {:type     "password"
                          :name     "password"
                          :value    password
                          :onChange #(comp/set-state! this {:password (.. % -target -value)})}))
            (div :.ui.error.message
              error)
            (div :.ui.field
              (dom/button :.ui.button
                {:onClick (fn [] (uism/trigger! this ::session/session :event/login {:username email
                                                                                     :password password}))
                 :classes [(when loading? "loading")]} "Login"))))))))

(def ui-login (comp/factory Login))

(defsc Main [this props]
  {:query           [:main/welcome-message]
   :initial-state   {:main/welcome-message "Hi!"}
   :ident           (fn [] [:screen/id :main])
   :route-segment   ["main"]
   :will-enter      (fn [_ _] (dr/route-immediate [:screen/id :main]))
   :route-cancelled (fn [p])}
  (dom/div "MAIN"))

(dr/defrouter TopRouter [this props]
  {:router-targets [Main Signup]})

(def ui-top-router (comp/factory TopRouter))

(defsc Session
  "Session representation. Used primarily for server queries. On-screen representation happens in Login component."
  [this {:keys [:session/valid? :account/name] :as props}]
  {:query         [:session/valid? :account/name]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (merge {:session/valid? false :account/name ""}
                      data-tree))
   :initial-state {:session/valid? false :account/name ""}})

(def ui-session (prim/factory Session))

(defsc Root [this {:root/keys [router current-session login]}]
  {:query         [{:root/router (comp/get-query TopRouter)}
                   {:root/current-session (comp/get-query Session)}
                   {:root/login (comp/get-query Login)}]
   :initial-state {:root/router          {}
                   :root/login           {}
                   :root/current-session {}}}
  (div :.ui.container.grid
    (div :.ui.row
      (div :.ui.four.column.grid
        (div :.ui.row
          (div :.three.columns "Welcome")
          (div :.column (ui-login login)))))
    (div :.ui.row
      (button {:onClick (fn [] (dr/change-route this ["main"]))} "Main")
      (button {:onClick (fn [] (dr/change-route this ["signup"]))} "Signup"))
    (div :.ui.row
      (ui-top-router router))))
