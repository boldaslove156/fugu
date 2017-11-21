(ns fugu.helpers
  (:require
   [taoensso.encore :as enc :include-macros true]))

(defn deep-merge
  [& maps]
  (apply enc/nested-merge-with
         (fn [x y]
           (enc/cond
             (and (map? x) (map? y))
             (deep-merge x y)

             (and (coll? x) (coll? y))
             (enc/into-all x y)

             :else y))
         maps))

(defn update-when
  [m k f & args]
  (if (contains? m k)
    (apply update m k f args)
    m))
