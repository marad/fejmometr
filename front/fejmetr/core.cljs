(ns fejmetr.core
  (:require [reagent.core :as r]))

(defonce person (r/atom ""))

(defn add-fame-form []
  [:div.row
   [:div.input-field.col.s10
    [:input#name {:type "text"
                  :value @person
                  :on-change #(reset! person (-> % .-target .-value))
                  }]
    [:label.active {:for "name"} "Dla kogo dodajesz fejm?"]]
   [:div.input-field.col.s2
    [:button.waves-effect.waves-light.btn
     {:on-click #(console.log "HELLO WORLD" @person)}
     "Dodaj"]]
   ])

(defn layout [title content]
  [:div.container [:h1 title]
   content])

(defn simple-example []
  (layout "Fejmetr"
          [:div (add-fame-form)]))

(r/render-component [simple-example]
                    (js/document.getElementById "app"))
