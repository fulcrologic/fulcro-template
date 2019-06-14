(ns app.ui.root
  (:require
    [app.model.session :as session]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button]]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]))


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
          (dom/input {:name "email" :value email :onChange #(m/set-string! this :account/email :event %)}))
        (div :.ui.field.row
          (dom/label {:htmlFor "password"} "Password")
          (dom/input {:type "password" :name "password" :value password :onChange #(m/set-string! this :account/password :event %)}))
        (div :.ui.field.row
          (dom/label {:htmlFor "password2"} "Repease Password")
          (dom/input {:type "password" :name "password2" :value password-again :onChange #(m/set-string! this :account/password-again :event %)}))))))

(defsc Login [this {:account/keys [email]
                    :ui/keys      [error loading?]}]
  {:query         [:ui/loading? :ui/error :account/email]
   :initial-state {:account/email "" :ui/loading? false :ui/error ""}
   :ident         (fn [] [:screen/id :login])
   :route-segment ["login"]
   :will-enter    (fn [_ _]
                    (dr/route-immediate [:screen/id :login]))}
  (let [password (or (comp/get-state this :password) "")]   ; c.l. state for security
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
             :classes [(when loading? "loading")]} "Login"))))))

(defsc Main [this props]
  {:query           [:main/welcome-message]
   :initial-state   {:main/welcome-message "Hi!"}
   :ident           (fn [] [:screen/id :main])
   :route-segment   ["main"]
   :will-enter      (fn [_ _] (dr/route-immediate [:screen/id :main]))
   ;:will-leave      (fn [_] true)
   :route-cancelled (fn [p])}
  (dom/div "MAIN"))

(dr/defrouter TopRouter [this props]
  {:router-targets [Main Signup Login]})

(def ui-top-router (comp/factory TopRouter))

(defsc Root [this {:root/keys [router current-session]}]
  {:query         [{:root/router (comp/get-query TopRouter)}
                   {:root/current-session (comp/get-query session/Session)}]
   :initial-state {:root/router          {}
                   :root/current-session {}}}
  (div :.ui.container.grid
    (div :.ui.row
      (div :.ui.four.column.grid
        (div :.ui.row
          (div :.column (session/ui-session current-session)))))
    (div :.ui.row
      (button {:onClick (fn [] (dr/change-route this ["main"]))} "Main")
      (button {:onClick (fn [] (dr/change-route this ["signup"]))} "Signup")
      (button {:onClick (fn [] (dr/change-route this ["login"]))} "Login"))
    (div :.ui.row
      (ui-top-router router))))
